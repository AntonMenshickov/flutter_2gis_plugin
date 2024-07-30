import 'package:flutter_test/flutter_test.dart';
import 'package:flutter_2gis_plugin/flutter_2gis_plugin.dart';
import 'package:flutter_2gis_plugin/flutter_2gis_plugin_platform_interface.dart';
import 'package:flutter_2gis_plugin/flutter_2gis_plugin_method_channel.dart';
import 'package:plugin_platform_interface/plugin_platform_interface.dart';

class MockFlutter2gisPluginPlatform
    with MockPlatformInterfaceMixin
    implements Flutter2gisPluginPlatform {

  @override
  Future<String?> getPlatformVersion() => Future.value('42');
}

void main() {
  final Flutter2gisPluginPlatform initialPlatform = Flutter2gisPluginPlatform.instance;

  test('$MethodChannelFlutter2gisPlugin is the default instance', () {
    expect(initialPlatform, isInstanceOf<MethodChannelFlutter2gisPlugin>());
  });

  test('getPlatformVersion', () async {
    Flutter2gisPlugin flutter2gisPlugin = Flutter2gisPlugin();
    MockFlutter2gisPluginPlatform fakePlatform = MockFlutter2gisPluginPlatform();
    Flutter2gisPluginPlatform.instance = fakePlatform;

    expect(await flutter2gisPlugin.getPlatformVersion(), '42');
  });
}
