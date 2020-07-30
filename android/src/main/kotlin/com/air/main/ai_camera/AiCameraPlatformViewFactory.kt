package com.air.main.ai_camera

import android.content.Context
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory

/**
 * <p>
 * @author air on 2020
 * </p>
 */
class AiCameraPlatformViewFactory(binaryMessenger: BinaryMessenger) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {
    /** BinaryMessenger */
    private val mBinaryMessenger = binaryMessenger;
    override fun create(context: Context?, viewId: Int, args: Any?): PlatformView {
        return AiCameraPlatformView(context = context, binaryMessenger = mBinaryMessenger);
    }

}