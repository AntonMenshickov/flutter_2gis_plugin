import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

import 'flutter_2gis_platform_interface.dart';

/// An implementation of [Flutter2gisPlatform] that uses method channels.
class MethodChannelFlutter2gis extends Flutter2gisPlatform {
  /// The method channel used to interact with the native platform.
  @visibleForTesting
  final methodChannel = const MethodChannel('flutter_2gis');

  @override
  Future<String?> getPlatformVersion() async {
    final version = await methodChannel.invokeMethod<String>('getPlatformVersion');
    return version;
  }
}
