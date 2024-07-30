package com.anton.flutter_2gis_plugin

import android.content.Context
import android.graphics.BitmapFactory
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import io.flutter.Log
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import ru.dgis.sdk.DGis
import ru.dgis.sdk.coordinates.Bearing
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.coordinates.Latitude
import ru.dgis.sdk.coordinates.Longitude
import ru.dgis.sdk.map.BearingSource
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.CameraZoomRestrictions
import ru.dgis.sdk.map.MapObjectManager
import ru.dgis.sdk.map.MapOptions
import ru.dgis.sdk.map.MapTheme
import ru.dgis.sdk.map.MapView
import ru.dgis.sdk.map.MyLocationController
import ru.dgis.sdk.map.MyLocationMapObjectSource
import ru.dgis.sdk.map.RouteMapObjectSource
import ru.dgis.sdk.map.RouteVisualizationType
import ru.dgis.sdk.map.ScreenDistance
import ru.dgis.sdk.map.ScreenPoint
import ru.dgis.sdk.map.Tilt
import ru.dgis.sdk.map.TouchEventsObserver
import ru.dgis.sdk.map.TrafficSource
import ru.dgis.sdk.map.Zoom
import ru.dgis.sdk.navigation.DefaultNavigationControls
import ru.dgis.sdk.navigation.NavigationManager
import ru.dgis.sdk.navigation.NavigationView
import ru.dgis.sdk.positioning.DefaultLocationSource
import ru.dgis.sdk.positioning.registerPlatformLocationSource
import ru.dgis.sdk.traffic.TrafficControl
import ru.dgis.sdk.map.Color
import ru.dgis.sdk.map.GlobalMapOptions
import ru.dgis.sdk.map.GraphicsApi
import ru.dgis.sdk.map.Image
import ru.dgis.sdk.map.MapObject
import ru.dgis.sdk.map.SimpleClusterObject
import ru.dgis.sdk.map.imageFromBitmap
import ru.dgis.sdk.positioning.LocationSource
import java.io.ByteArrayInputStream


internal class GisMapView(
    context: Context,
    creationParams: Map<String?, Any?>?,
    messenger: BinaryMessenger,
) : PlatformView, MethodChannel.MethodCallHandler, androidx.lifecycle.DefaultLifecycleObserver {
    private val methodChannelName: String = "fgis"
    private var sdkContext: ru.dgis.sdk.Context
    private var methodChannel: MethodChannel
    private var gisView: MapView
    private lateinit var mapObjectManager: MapObjectManager
    private var controller: GisMapController
    private var navigationManager: NavigationManager
    private var routeMapObjectSource: RouteMapObjectSource
    private var trafficSource: TrafficSource
    private var myLocationMapObjectSource: MyLocationMapObjectSource
    private var locationSource: LocationSource
    private var navigationView: NavigationView
    private var trafficControl: TrafficControl
    private var navigationControls: DefaultNavigationControls
    private var trafficControlContainer: LinearLayout
    private lateinit var map: ru.dgis.sdk.map.Map
    private var mapInitialized: Boolean = false

    override fun getView(): View {
        return gisView
    }

    override fun dispose() {
        methodChannel.setMethodCallHandler(null)
    }

    init {
        methodChannel = MethodChannel(messenger, methodChannelName)
        methodChannel.setMethodCallHandler(this)

        sdkContext = DGis.initialize(context, mapOptions = GlobalMapOptions(GraphicsApi.VULKAN))
        locationSource = DefaultLocationSource(context)
        registerPlatformLocationSource(sdkContext, locationSource)

        val mapOptions = MapOptions()
        mapOptions.position = CameraPosition(
            point = GeoPoint(
                latitude = Latitude(creationParams?.get("latitude") as Double),
                longitude = Longitude(creationParams["longitude"] as Double),
            ),
            zoom = Zoom((creationParams["zoom"] as Double).toFloat()),
            tilt = Tilt((creationParams["tilt"] as Double).toFloat()),
            bearing = Bearing((creationParams["bearing"] as Double)),
        )
        mapOptions.setTheme(MapTheme("day", Color(255, 255, 255)))

        gisView = MapView(context, mapOptions)

        navigationManager = NavigationManager(sdkContext)
        routeMapObjectSource = RouteMapObjectSource(sdkContext, RouteVisualizationType.NORMAL)

        val nm = navigationManager
        trafficSource = TrafficSource(sdkContext)
        myLocationMapObjectSource = MyLocationMapObjectSource(
            sdkContext, MyLocationController(BearingSource.SATELLITE)
        )

        navigationView = NavigationView(context)
        trafficControl = TrafficControl(context, null, 0)
        navigationControls = DefaultNavigationControls(navigationView.context, null, 0)

        trafficControlContainer = LinearLayout(context)
        trafficControlContainer.setPadding(15, 0, 0, 15)
        trafficControlContainer.gravity = Gravity.BOTTOM
        trafficControlContainer.addView(trafficControl)

        gisView.getMapAsync { map ->
            this.map = map
            mapObjectManager = MapObjectManager(map)
            this.mapInitialized = true
            gisView.setTouchEventsObserver(object : TouchEventsObserver {
                override fun onTap(point: ScreenPoint) {
                    map.getRenderedObjects(point, ScreenDistance(1f))
                        .onResult { renderedObjectInfos ->
                            for (renderedObjectInfo in renderedObjectInfos) {

                                val currentObject: MapObject =
                                    if (renderedObjectInfo.item.item is SimpleClusterObject) {
                                        (renderedObjectInfo.item.item as SimpleClusterObject).objects.first();
                                    } else {
                                        renderedObjectInfo.item.item
                                    }
                                if (currentObject.userData != null) {
                                    val args = mapOf(
                                        "id" to currentObject.userData
                                    )
                                    Log.d("DGIS", "Tap on marker ${currentObject.userData}")
                                    methodChannel.invokeMethod(
                                        "ontap_marker", args
                                    )
                                    break
                                }
                            }
                        }
                    super.onTap(point)
                }
            })
            map.camera.zoomRestrictions = CameraZoomRestrictions(
                minZoom = Zoom(value = 3.0f), maxZoom = Zoom(value = 20.0f)
            )
            map.addSource(myLocationMapObjectSource)
            map.addSource(routeMapObjectSource)
            map.addSource(trafficSource)


            navigationView.addView(navigationControls)
            navigationView.apply {
                navigationManager = nm
            }
            gisView.addView(trafficControlContainer);
        }

        controller = GisMapController(gisView, sdkContext, navigationManager, routeMapObjectSource)
    }


    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        when (call.method) {
            "getCameraPosition" -> {
                controller.getCameraPosition(result = result)
            }

            "setCameraPosition" -> {
                controller.setCameraPosition(call = call, result = result)
            }

            "updateMarkers" -> {
                if (!mapInitialized) {
                    return result.error("Map not initialized", "", "");
                }
                val args = call.arguments as Map<String, Any>;
                val markers = args["markers"] as List<Map<String, Any>>
                val newIconSet: Map<String, Image> = buildMap {
                    val icons = args["icons"] as Map<String, ByteArray>
                    icons.forEach { (s, bytes) ->
                        this[s] = imageFromBitmap(
                            sdkContext, BitmapFactory.decodeStream(
                                ByteArrayInputStream(bytes)
                            )
                        )
                    }
                }
                mapObjectManager.removeAll()
                controller.updateMarkers(
                    markers, newIconSet, mapObjectManager
                )
            }

            "setRoute" -> {
                controller.buildRoute(arguments = call.arguments, result = result)
            }

            "removeRoute" -> {
                controller.removeRoute(result = result)
            }

            "startNavigation" -> {
                if (navigationView.parent == null) {
                    gisView.addView(navigationView)
                }
                if (trafficControlContainer.parent != null) {
                    gisView.removeView(trafficControlContainer)
                }
                controller.startNavigation(
                    arguments = call.arguments, result = result
                )
            }

            "stopNavigation" -> {
                controller.stopNavigation(result = result)
                if (navigationView.parent != null) {
                    gisView.removeView(navigationView)
                }
                if (trafficControlContainer.parent == null) {
                    gisView.addView(trafficControlContainer)
                }
            }
        }
    }

}