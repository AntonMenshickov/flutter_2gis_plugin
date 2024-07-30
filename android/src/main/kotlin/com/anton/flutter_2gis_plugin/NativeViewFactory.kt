package com.anton.flutter_2gis_plugin

import android.content.Context
import androidx.lifecycle.setViewTreeLifecycleOwner
import io.flutter.embedding.android.FlutterActivity
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

class NativeViewFactory(messenger: BinaryMessenger, private val activity: FlutterActivity) :
    PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    private val mess = messenger

    override fun create(context: Context, viewId: Int, args: Any?): PlatformView {
        val creationParams = args as Map<String?, Any?>?
        val view = GisMapView(context, creationParams, mess)
        view.view.setViewTreeLifecycleOwner(activity)
        activity.lifecycle.addObserver(view);
        return view;
    }
}