import 'dart:typed_data';

class GisMapObjectsSet {
  final Map<String, Uint8List> icons;
  final List<GisMapMarker> markers;

  GisMapObjectsSet({
    required this.icons,
    required this.markers,
  });

  Map<String, dynamic> toJson() => {
        'icons': icons,
        'markers': markers.map((e) => e.toJson()).toList(),
      };
}

class GisMapMarker {
  final double latitude;
  final double longitude;
  final String iconName;
  final int zIndex;
  final String id;
  final String? layer;

  GisMapMarker({
    required this.latitude,
    required this.longitude,
    required this.iconName,
    required this.id,
    required this.zIndex,
    this.layer,
  });

  Map<String, dynamic> toJson() => {
        'latitude': latitude,
        'longitude': longitude,
        'iconName': iconName,
        'id': id,
        'zIndex': zIndex,
        'layer': layer,
      };
}

class RouteOptions {
  final double startLatitude;
  final double startLongitude;
  final double endLatitude;
  final double endLongitude;
  final bool? avoidTollRoads;
  final bool? avoidUnpavedRoads;
  final bool? avoidFerries;
  final int? truckHeight;
  final int? truckWidth;
  final int? truckLength;
  final int? maxPermittedMass;
  final int? actualMass;
  final bool? dangerousCargo;
  final bool? explosiveCargo;
  final bool? pedestrian;

  RouteOptions({
    required this.startLatitude,
    required this.startLongitude,
    required this.endLatitude,
    required this.endLongitude,
    this.avoidTollRoads,
    this.avoidUnpavedRoads,
    this.avoidFerries,
    this.truckHeight,
    this.truckWidth,
    this.truckLength,
    this.maxPermittedMass,
    this.actualMass,
    this.dangerousCargo,
    this.explosiveCargo,
    this.pedestrian,
  });

  Map<String, dynamic> toJson() => {
        "startLatitude": startLatitude,
        "startLongitude": startLongitude,
        "endLatitude": endLatitude,
        "endLongitude": endLongitude,
        if (avoidTollRoads != null) "avoidTollRoads": avoidTollRoads,
        if (avoidUnpavedRoads != null) "avoidUnpavedRoads": avoidUnpavedRoads,
        if (avoidFerries != null) "avoidFerries": avoidFerries,
        if (truckHeight != null) "truckHeight": truckHeight,
        if (truckWidth != null) "truckWidth": truckWidth,
        if (truckLength != null) "truckLength": truckLength,
        if (maxPermittedMass != null) "maxPermittedMass": maxPermittedMass,
        if (actualMass != null) "actualMass": actualMass,
        if (dangerousCargo != null) "dangerousCargo": dangerousCargo,
        if (explosiveCargo != null) "explosiveCargo": explosiveCargo,
        if (pedestrian != null) "pedestrian": pedestrian,
      };
}
