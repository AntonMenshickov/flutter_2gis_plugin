import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'flutter_2gis_method_channel.dart';

abstract class Flutter2gisPlatform extends PlatformInterface {
  /// Constructs a Flutter2gisPlatform.
  Flutter2gisPlatform() : super(token: _token);

  static final Object _token = Object();

  static Flutter2gisPlatform _instance = MethodChannelFlutter2gis();

  /// The default instance of [Flutter2gisPlatform] to use.
  ///
  /// Defaults to [MethodChannelFlutter2gis].
  static Flutter2gisPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [Flutter2gisPlatform] when
  /// they register themselves.
  static set instance(Flutter2gisPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
