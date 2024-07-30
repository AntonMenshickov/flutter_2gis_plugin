import 'dart:async';
import 'dart:developer';
import 'package:flutter/services.dart';
import 'package:flutter_2gis_plugin/model/gis_camera_position.dart';
import 'package:flutter_2gis_plugin/model/gis_map_object.dart';

class GisMapController {
  GisMapController();

  late GisMapObjectsSet mapObjects;
  final _platform = const MethodChannel('fgis');

  GisCameraPosition? position;

  Future<void> init() async {
    position = await getCameraPosition();
  }

  Future<GisCameraPosition> getCameraPosition() async {
    try {
      final result = await _platform.invokeMethod('getCameraPosition');
      return GisCameraPosition.fromJson(result);
    } on PlatformException catch (e) {
      // ignore: avoid_print
      print('Platform exeption getCameraPosition() message: $e');
    }

    return const GisCameraPosition(latitude: 0.0, longitude: 0.0);
  }

  Future<String> setCameraPosition(
      {required GisCameraPosition position, int? duration}) async {
    try {
      final String status = await _platform.invokeMethod('setCameraPosition',
          position.toNativeMap()..addAll({'duration': duration ?? 0}));
      return status;
    } on PlatformException catch (e) {
      // ignore: avoid_print
      print('Platform exeption setCameraPosition() message: $e');
      return "ERROR";
    }
  }

  Future<String> increaseZoom({int? duration, int? size}) async {
    try {
      final position = await getCameraPosition();
      final String status = await _platform.invokeMethod(
          'setCameraPosition',
          position.copyWith(zoom: position.zoom + (size ?? 1)).toNativeMap()
            ..addAll({'duration': duration ?? 0}));
      return status;
    } on PlatformException catch (e) {
      // ignore: avoid_print
      print('Platform exeption setCameraPosition() message: $e');
      return "ERROR";
    }
  }

  Future<String> reduceZoom({int? duration, int? size}) async {
    try {
      final position = await getCameraPosition();
      final String status = await _platform.invokeMethod(
          'setCameraPosition',
          position
              .copyWith(
                  zoom: position.zoom - (size ?? 1) < 0
                      ? 3.0
                      : position.zoom - (size ?? 1))
              .toNativeMap()
            ..addAll({'duration': duration ?? 0}));
      return status;
    } on PlatformException catch (e) {
      // ignore: avoid_print
      print('Platform exeption setCameraPosition() message: $e');
      return "ERROR";
    }
  }

  Future<void> updateMarkers(GisMapObjectsSet markers) async {
    try {
      mapObjects = markers;
      await _platform.invokeMethod('updateMarkers', markers.toJson());
    } on PlatformException catch (e) {
      log('Platform exeption updateMarkers() message: $e');
    }
  }

  Future<String> setRoute(RouteOptions position) async {
    try {
      final String status =
          await _platform.invokeMethod('setRoute', position.toJson());
      return status;
    } on PlatformException catch (e) {
      log('Platform exeption setRoute() message: $e');
      return "ERROR";
    }
  }

  Future<String> removeRoute() async {
    try {
      final String status = await _platform.invokeMethod('removeRoute');
      return status;
    } on PlatformException catch (e) {
      log('Platform exeption removeRoute() message: $e');
      return "ERROR";
    }
  }

  Future<String> startNavigation(RouteOptions option) async {
    try {
      String status =
          await _platform.invokeMethod('startNavigation', option.toJson());
      return status;
    } on PlatformException catch (e) {
      log('Platform exeption startNavigation() message: $e');
      return 'ERROR';
    }
  }

  Future<String> stopNavigation() async {
    try {
      String status = await _platform.invokeMethod('stopNavigation');
      return status;
    } on PlatformException catch (e) {
      log('Platform exeption stopNavigation() message: $e');
      return 'ERROR';
    }
  }
}
