package com.air.main.ai_camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.ImageFormat
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView

/**
 * <p>
 * @author air on 2020
 * </p>
 */
class AiCameraSelectorPlatformView(context: Context?, binaryMessenger: BinaryMessenger) : PlatformView, MethodChannel.MethodCallHandler, LifecycleOwner {
    /** Context */
    private val mContext: Context = context!!;
    
    private val mCameraRecyclerView = RecyclerView(mContext);

    /** MethodChannel */
    private val mMethodChannel: MethodChannel = MethodChannel(binaryMessenger, GlobalConfig.METHOD_CHANNEL_NAME_CAMERA_SELECTOR_PLATFORM_VIEW)


    companion object {

        /** Helper class used as a data holder for each selectable camera format item */
        private data class FormatItem(val title: String, val cameraId: String, val format: Int)

        /** Helper function used to convert a lens orientation enum into a human-readable string */
        private fun lensOrientationString(value: Int) = when(value) {
            CameraCharacteristics.LENS_FACING_BACK -> "后置"
            CameraCharacteristics.LENS_FACING_FRONT -> "前置"
            CameraCharacteristics.LENS_FACING_EXTERNAL -> "外部"
            else -> "未知"
        }

        /** Helper function used to list all compatible cameras and supported pixel formats */
        @SuppressLint("InlinedApi")
        private fun enumerateCameras(cameraManager: CameraManager): List<FormatItem> {
            val availableCameras: MutableList<FormatItem> = mutableListOf()

            // Get list of all compatible cameras
            val cameraIds = cameraManager.cameraIdList.filter {
                val characteristics = cameraManager.getCameraCharacteristics(it)
                val capabilities = characteristics.get(
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)
                capabilities?.contains(
                        CameraMetadata.REQUEST_AVAILABLE_CAPABILITIES_BACKWARD_COMPATIBLE) ?: false
            }


            // Iterate over the list of cameras and return all the compatible ones
            cameraIds.forEach { id ->
                val characteristics = cameraManager.getCameraCharacteristics(id)
                val orientation = lensOrientationString(
                        characteristics.get(CameraCharacteristics.LENS_FACING)!!)

                // Query the available capabilities and output formats
                val capabilities = characteristics.get(
                        CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES)!!
                val outputFormats = characteristics.get(
                        CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!.outputFormats

                // All cameras *must* support JPEG output so we don't need to check characteristics
                availableCameras.add(FormatItem(
                        "$orientation JPEG ($id)", id, ImageFormat.JPEG))

                // Return cameras that support RAW capability
                if (capabilities.contains(
                                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_RAW) &&
                        outputFormats.contains(ImageFormat.RAW_SENSOR)) {
                    availableCameras.add(FormatItem(
                            "$orientation RAW ($id)", id, ImageFormat.RAW_SENSOR))
                }

                // Return cameras that support JPEG DEPTH capability
                if (capabilities.contains(
                                CameraCharacteristics.REQUEST_AVAILABLE_CAPABILITIES_DEPTH_OUTPUT) &&
                        outputFormats.contains(ImageFormat.DEPTH_JPEG)) {
                    availableCameras.add(FormatItem(
                            "$orientation DEPTH ($id)", id, ImageFormat.DEPTH_JPEG))
                }
            }

            return availableCameras
        }
    }
    
    init {
        //MethodCallHandler
        mMethodChannel.setMethodCallHandler(this);
        
        configCameraListView();
    }

    private fun configCameraListView() {

        mCameraRecyclerView.apply {
            layoutManager = LinearLayoutManager(mContext);

            val cameraManager =
                    mContext.getSystemService(Context.CAMERA_SERVICE) as CameraManager

            val cameraList = enumerateCameras(cameraManager)

            val layoutId = android.R.layout.simple_list_item_1
            adapter = GenericListAdapter(cameraList, itemLayoutId = layoutId) { view, item, _ ->
                view.findViewById<TextView>(android.R.id.text1).text = item.title
                view.setOnClickListener {
                    
                    mMethodChannel.invokeMethod("ai_camera_selector_result", mutableMapOf(
                            Pair("cameraId", item.cameraId),
                            Pair("format", item.format)
                    ));
//                    Navigation.findNavController(requireActivity(), R.id.fragment_container)
//                            .navigate(SelectorFragmentDirections.actionSelectorToCamera(
//                                    item.cameraId, item.format))
                }
            }
        }
    }


    override fun getView(): View {
        return mCameraRecyclerView;
    }

    override fun dispose() {

    }

    /** MethodCall */
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        val method: String = call.method;

        val test = call.argument<String>("keyName");
    }

    override fun getLifecycle(): Lifecycle {
        return LifecycleImpl();
    }

}

class LifecycleImpl : Lifecycle() {
    override fun addObserver(observer: LifecycleObserver) {
    }

    override fun removeObserver(observer: LifecycleObserver) {
    }

    override fun getCurrentState(): State {
        return State.INITIALIZED
    }

} 
