package com.anton.flutter_2gis_plugin

import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.BinaryMessenger

/** Flutter2gisPlugin */
class Flutter2gisPlugin : FlutterPlugin, ActivityAware {
    private val viewTypeId: String = "<dgis-view-flutter>"
    private var activity: FlutterActivity? = null
    private var binaryMessenger: BinaryMessenger? = null
    private var pluginBinding: FlutterPlugin.FlutterPluginBinding? = null


    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        pluginBinding = binding;
        binaryMessenger = binding.binaryMessenger;
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        onDetach();
    }

    override fun onAttachedToActivity(binding: ActivityPluginBinding) {
        activity = binding.activity as FlutterActivity
        registerPlatformViewFactory();
    }

    override fun onDetachedFromActivityForConfigChanges() {
        activity = null
    }

    override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
        activity = binding.activity as FlutterActivity
        registerPlatformViewFactory();
    }

    override fun onDetachedFromActivity() {
        onDetach();
    }

    private fun onDetach() {
        pluginBinding = null
        binaryMessenger = null
    }
    private fun registerPlatformViewFactory() {
        pluginBinding!!
            .platformViewRegistry
            .registerViewFactory(viewTypeId, NativeViewFactory(binaryMessenger!!, activity!!))
    }

}
