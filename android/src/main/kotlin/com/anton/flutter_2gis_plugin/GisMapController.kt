package com.anton.flutter_2gis_plugin

import io.flutter.Log
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import ru.dgis.sdk.Context
import ru.dgis.sdk.Duration
import ru.dgis.sdk.coordinates.Bearing
import ru.dgis.sdk.coordinates.GeoPoint
import ru.dgis.sdk.coordinates.Latitude
import ru.dgis.sdk.coordinates.Longitude
import ru.dgis.sdk.geometry.ComplexGeometry
import ru.dgis.sdk.geometry.GeoPointWithElevation
import ru.dgis.sdk.geometry.PointGeometry
import ru.dgis.sdk.map.CameraAnimationType
import ru.dgis.sdk.map.CameraPosition
import ru.dgis.sdk.map.Image
import ru.dgis.sdk.map.MapObjectManager
import ru.dgis.sdk.map.MapView
import ru.dgis.sdk.map.Marker
import ru.dgis.sdk.map.MarkerOptions
import ru.dgis.sdk.map.Padding
import ru.dgis.sdk.map.RouteMapObject
import ru.dgis.sdk.map.RouteMapObjectSource
import ru.dgis.sdk.map.SimpleMapObject
import ru.dgis.sdk.map.Tilt
import ru.dgis.sdk.map.ZIndex
import ru.dgis.sdk.map.Zoom
import ru.dgis.sdk.map.calcPosition
import ru.dgis.sdk.navigation.NavigationManager
import ru.dgis.sdk.navigation.RouteBuildOptions
import ru.dgis.sdk.routing.CarRouteSearchOptions
import ru.dgis.sdk.routing.PedestrianRouteSearchOptions
import ru.dgis.sdk.routing.RouteIndex
import ru.dgis.sdk.routing.RouteSearchOptions
import ru.dgis.sdk.routing.RouteSearchPoint
import ru.dgis.sdk.routing.RouteSearchType
import ru.dgis.sdk.routing.TrafficRoute
import ru.dgis.sdk.routing.TrafficRouter
import ru.dgis.sdk.routing.TruckRouteSearchOptions

class GisMapController(gv: MapView, ctx: Context, nm: NavigationManager, rm: RouteMapObjectSource) {

    private var gisView = gv
    private var sdkContext = ctx
    private var navigationManager = nm
    private var routeMapObjectSource = rm
    private var currentRouteMapObject: RouteMapObject? = null


    fun setCameraPosition(call: MethodCall, result: MethodChannel.Result) {
        val args: Map<String, Any?> = call.arguments as Map<String, Any?>
        val cameraPosition = CameraPosition(
            GeoPoint(
                latitude = Latitude(value = args["latitude"] as Double),
                longitude = Longitude(value = args["longitude"] as Double)
            ),
            zoom = Zoom(value = (args["zoom"] as Double).toFloat()),
            bearing = Bearing(value = args["bearing"] as Double),
            tilt = Tilt(value = (args["tilt"] as Double).toFloat())
        )
        gisView.getMapAsync { map ->
            map.camera.move(
                cameraPosition,
                Duration.ofMilliseconds((args["duration"] as Int).toLong()),
                CameraAnimationType.LINEAR
            ).onResult {
                Log.d("APP", "Перелёт камеры завершён.")
                result.success("OK")
            }
        }
    }

    fun getCameraPosition(result: MethodChannel.Result) {
        lateinit var cameraPosition: CameraPosition;
        gisView.getMapAsync { map ->
            cameraPosition = map.camera.position;
            val data = mapOf(
                "latitude" to cameraPosition.point.latitude.value,
                "longitude" to cameraPosition.point.longitude.value,
                "bearing" to cameraPosition.bearing.value,
                "tilt" to cameraPosition.tilt.value,
                "zoom" to cameraPosition.zoom.value,
            )
            result.success(data);
        }
    }

    fun updateMarkers(
        markers: List<Map<String, Any>>,
        iconSet: Map<String, Image>,
        mapObjectManager: MapObjectManager
    ) {
        val objects: MutableList<SimpleMapObject> = ArrayList();
        for (i in markers) {
            val icon = iconSet[i["iconName"] as String]
            val marker = Marker(
                MarkerOptions(
                    position = GeoPointWithElevation(
                        latitude = i["latitude"] as Double,
                        longitude = i["longitude"] as Double,
                    ),
                    icon = icon,
                    zIndex = ZIndex(i["zIndex"] as Int),
                    userData = i["id"],
                )
            )
            objects.add(marker)
        }
        mapObjectManager.addObjects(objects.toList());

    }

    fun buildRoute(
        arguments: Any, result: MethodChannel.Result
    ) {
        if (currentRouteMapObject != null) {
            routeMapObjectSource.removeObject(currentRouteMapObject!!)
            currentRouteMapObject!!.close()
            currentRouteMapObject = null;
        }
        val args = arguments as Map<String, Any>
        val startPoint = GeoPoint(
            latitude = args["startLatitude"] as Double, longitude = args["startLongitude"] as Double
        )
        val endPoint = GeoPoint(
            latitude = args["endLatitude"] as Double, longitude = args["endLongitude"] as Double
        )
        val startSearchPoint = RouteSearchPoint(
            coordinates = startPoint
        )
        val finishSearchPoint = RouteSearchPoint(
            coordinates = endPoint
        )

        val trafficRouter = TrafficRouter(sdkContext)
        val routesFuture =
            trafficRouter.findRoute(startSearchPoint, finishSearchPoint, buildSearchOptions(args))
        removeRouteFromMap();
        routesFuture.onResult { routes: List<TrafficRoute> ->
            if (routes.isNotEmpty()) {
                val route = routes.first();
                currentRouteMapObject = RouteMapObject(route, true, RouteIndex(0))
                routeMapObjectSource.addObject(currentRouteMapObject!!)
                val geometry =
                    ComplexGeometry(route.route.geometry.entries.map { PointGeometry(it.value) })
                gisView.getMapAsync { map ->
                    val position = calcPosition(
                        map.camera,
                        geometry,
                        screenArea = Padding(top = 50, bottom = 50, left = 50, right = 50),
                        tilt = Tilt(0.0f),
                        bearing = Bearing(0.0),
                    )
                    map.camera.move(
                        position, Duration.ofMilliseconds(200), CameraAnimationType.LINEAR
                    )
                }

                result.success("OK")
            } else {
                result.error("Failed to find route", "Can`t build route for this points", "")
            }
        }
    }

    fun removeRoute(result: MethodChannel.Result) {
        removeRouteFromMap();
        result.success("OK")
    }

    fun startNavigation(arguments: Any, result: MethodChannel.Result) {
        val args = arguments as Map<String, Any>
        val startPoint = GeoPoint(
            latitude = args["startLatitude"] as Double, longitude = args["startLongitude"] as Double
        )
        val endPoint = GeoPoint(
            latitude = args["endLatitude"] as Double, longitude = args["endLongitude"] as Double
        )
        val startSearchPoint = RouteSearchPoint(
            coordinates = startPoint
        )
        val finishSearchPoint = RouteSearchPoint(
            coordinates = endPoint
        )

        val routeSearchOptions = buildSearchOptions(args)
        val trafficRouter = TrafficRouter(sdkContext)
        val routesFuture =
            trafficRouter.findRoute(startSearchPoint, finishSearchPoint, routeSearchOptions)
        routesFuture.onResult { routes: List<TrafficRoute> ->
            if (routes.isNotEmpty()) {
                val route = routes.first();
                val routeBuildOptions = RouteBuildOptions(
                    finishPoint = RouteSearchPoint(
                        coordinates = GeoPoint(
                            latitude = args["endLatitude"] as Double,
                            longitude = args["endLongitude"] as Double
                        )
                    ), routeSearchOptions = routeSearchOptions
                )
                navigationManager.start(routeBuildOptions, route)
                result.success("OK");
            } else {
                result.error("Failed to find route", "Can`t build route for this points", "")
            }
        }

    }

    fun stopNavigation(result: MethodChannel.Result) {
        navigationManager.stop()

        gisView.getMapAsync { map ->
            map.camera.move(
                CameraPosition(
                    map.camera.position.point,
                    map.camera.position.zoom,
                    bearing = Bearing(0.0),
                    tilt = Tilt(0.0f)
                ), Duration.ofMilliseconds(200), CameraAnimationType.LINEAR
            )
        }
        result.success("OK");
    }

    private fun removeRouteFromMap() {
        if (currentRouteMapObject != null) {
            routeMapObjectSource.removeObject(currentRouteMapObject!!)
            currentRouteMapObject!!.close()
            currentRouteMapObject = null;
        }
    }

    private fun buildSearchOptions(args: Map<String, Any>): RouteSearchOptions {
        if (args.containsKey("pedestrian") && (args["pedestrian"] as Boolean)) {
            return RouteSearchOptions(
                PedestrianRouteSearchOptions(
                    useIndoor = false
                ),
            )
        }
        return RouteSearchOptions(
            truck = TruckRouteSearchOptions(
                car = CarRouteSearchOptions(
                    avoidTollRoads = if (args.containsKey("avoidTollRoads")) (args["avoidTollRoads"] as Boolean) else true,
                    avoidUnpavedRoads = if (args.containsKey("avoidUnpavedRoads")) (args["avoidUnpavedRoads"] as Boolean) else false,
                    avoidFerries = if (args.containsKey("avoidFerries")) (args["avoidFerries"] as Boolean) else true,
                    routeSearchType = RouteSearchType.JAM
                ),
                truckHeight = if (args.containsKey("truckHeight")) (args["truckHeight"] as Int) else 3200,
                truckWidth = if (args.containsKey("truckWidth")) (args["truckWidth"] as Int) else 2500,
                truckLength = if (args.containsKey("truckLength")) (args["truckLength"] as Int) else 7000,
                maxPermittedMass = if (args.containsKey("maxPermittedMass")) (args["maxPermittedMass"] as Int) else 20000,
                actualMass = if (args.containsKey("actualMass")) (args["actualMass"] as Int) else 12000,
                dangerousCargo = if (args.containsKey("dangerousCargo")) (args["dangerousCargo"] as Boolean) else false,
                explosiveCargo = if (args.containsKey("explosiveCargo")) (args["explosiveCargo"] as Boolean) else false,
            )
        )
    }
}

