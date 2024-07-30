
import 'flutter_2gis_platform_interface.dart';

class Flutter2gis {
  Future<String?> getPlatformVersion() {
    return Flutter2gisPlatform.instance.getPlatformVersion();
  }
}
