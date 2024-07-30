import 'dart:async';

import 'package:flutter/material.dart';
import 'package:flutter_2gis_plugin/gis_map_controller.dart';
import 'package:flutter_2gis_plugin/model/gis_camera_position.dart';
import 'package:flutter_2gis_plugin/model/gis_map_object.dart';

import 'package:flutter_2gis_plugin/view/dgis_map_view.dart';
import 'package:geolocator/geolocator.dart';

void main() {
  runApp(const MaterialApp(home: MyApp()));
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  late final GisMapController controller;
  StreamSubscription? positionStream;
  Position? currentPosition;
  bool routeLoading = false;
  bool navigatorLoading = false;
  bool routeEnabled = false;
  bool navigatorEnabled = false;

  @override
  void initState() {
    controller = GisMapController();
    _setUpLocationListener();
    super.initState();
  }

  @override
  void dispose() {
    positionStream?.cancel();
    super.dispose();
  }

  void _setUpLocationListener() async {
    final permission = await Geolocator.checkPermission();
    if (permission == LocationPermission.denied) {
      await Geolocator.requestPermission();
    }
    const LocationSettings locationSettings = LocationSettings(
      accuracy: LocationAccuracy.high,
      distanceFilter: 100,
    );

    positionStream =
        Geolocator.getPositionStream(locationSettings: locationSettings)
            .listen((Position? position) {
          setState(() {
            currentPosition = position;
          });
        });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Column(
        children: [
          Expanded(
            child: DGisMapView(
              controller: controller,
              startCameraPosition:
              const GisCameraPosition(latitude: 0.0, longitude: 0.0),
              onTapMarker: (marker) {
                print(marker);
              },
              typeView: TypeView.platformView,
            ),
          ),
        ],
      ),
      floatingActionButton: Row(
        mainAxisAlignment: MainAxisAlignment.end,
        children: [
          _buildActionButton(
            icon: Icons.my_location_rounded,
            onPressed: () async {
              await controller.setCameraPosition(
                  position: GisCameraPosition(
                    latitude: currentPosition!.latitude,
                    longitude: currentPosition!.longitude,
                    zoom: 15,
                    bearing: 0,
                    tilt: 0,
                  ),
                  duration: 200);
            },
            disabled: currentPosition == null,
          ),
          _buildActionButton(
            icon: Icons.zoom_in_outlined,
            onPressed: () async {
              await controller.increaseZoom(duration: 200);
            },
          ),
          _buildActionButton(
            icon: Icons.zoom_out_outlined,
            onPressed: () async {
              await controller.reduceZoom(duration: 200);
            },
          ),
          _buildActionButton(
            icon: Icons.route,
            onPressed: () async {
              if (routeEnabled) {
                await controller.removeRoute();
                setState(() {
                  routeEnabled = false;
                });
                return;
              }
              setState(() {
                routeLoading = true;
              });
              final result =
              await controller.setRoute(await _getRouteOptions());
              setState(() {
                routeLoading = false;
                routeEnabled = result == "OK";
              });
            },
            disabled: currentPosition == null,
            loading: routeLoading,
            enabled: routeEnabled,
          ),
          _buildActionButton(
            icon: Icons.navigation_outlined,
            onPressed: () async {
              if (navigatorEnabled) {
                await controller.stopNavigation();
                setState(() {
                  navigatorEnabled = false;
                });
                return;
              }
              setState(() {
                navigatorLoading = true;
              });
              final result =
              await controller.startNavigation(await _getRouteOptions());
              setState(() {
                navigatorLoading = false;
                navigatorEnabled = result == "OK";
              });
            },
            disabled: currentPosition == null,
            loading: navigatorLoading,
            enabled: navigatorEnabled,
          ),
        ],
      ),
    );
  }

  Future<RouteOptions> _getRouteOptions() async {
    final (double, double) finishPoint = await _showGeoPointInputDialog();
    return RouteOptions(
      startLatitude: currentPosition!.latitude,
      startLongitude: currentPosition!.longitude,
      endLatitude: finishPoint.$1,
      endLongitude: finishPoint.$2,
    );
  }

  Widget _buildActionButton({
    required IconData icon,
    required VoidCallback onPressed,
    bool disabled = false,
    bool loading = false,
    bool enabled = false,
  }) =>
      Padding(
        padding: const EdgeInsets.all(8.0),
        child: FloatingActionButton(
          backgroundColor: enabled ? Colors.green.shade300 : null,
          onPressed: () => disabled ? null : onPressed(),
          child: Opacity(
              opacity: disabled ? 0.5 : 1.0,
              child: loading
                  ? const Padding(
                padding: EdgeInsets.all(16.0),
                child: CircularProgressIndicator(),
              )
                  : Icon(icon)),
        ),
      );

  Future<(double, double)> _showGeoPointInputDialog() async {
    final latitudeController = TextEditingController(text: "52.3409");
    final longitudeController = TextEditingController(text: "104.2678");
    final textTheme = Theme.of(context).textTheme;
    final result = await showDialog(
        context: context,
        barrierDismissible: false,
        builder: (context) {
          return Dialog(
            child: SizedBox(
              width: 360.0,
              height: 240.0,
              child: Padding(
                padding: const EdgeInsets.all(16.0),
                child: Column(
                  children: [
                    Text(
                      "Enter finish point location",
                      style: textTheme.titleMedium,
                    ),
                    SizedBox(
                      width: 360.0,
                      child: TextField(
                        controller: latitudeController,
                        decoration: const InputDecoration(hintText: 'Latitude'),
                      ),
                    ),
                    SizedBox(
                      width: 360.0,
                      child: TextField(
                        controller: longitudeController,
                        decoration:
                        const InputDecoration(hintText: 'Longitude'),
                      ),
                    ),
                    const Expanded(child: SizedBox()),
                    Align(
                      alignment: Alignment.centerRight,
                      child: TextButton(
                        onPressed: () => Navigator.of(context).pop((
                        double.parse(latitudeController.text),
                        double.parse(longitudeController.text)
                        )),
                        child: const Text("Select"),
                      ),
                    )
                  ],
                ),
              ),
            ),
          );
        });
    return result;
  }
}
