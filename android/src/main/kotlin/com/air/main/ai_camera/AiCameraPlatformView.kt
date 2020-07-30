package com.air.main.ai_camera

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.ImageFormat
import android.graphics.drawable.ColorDrawable
import android.hardware.camera2.*
import android.media.ExifInterface
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.PowerManager
import android.util.Log
import android.view.LayoutInflater
import android.view.Surface
import android.view.SurfaceHolder
import android.view.View
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.platform.PlatformView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.Closeable
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 * <p>
 * @author air on 2020
 * {@link #takePhoto()}
 * </p>
 */
class AiCameraPlatformView(context: Context?, binaryMessenger: BinaryMessenger) : PlatformView, MethodChannel.MethodCallHandler, LifecycleOwner {
    /** Context */
    private val mContext: Context = context!!;

    private val mRootView: View;

    /** MethodChannel */
    private val mMethodChannel: MethodChannel = MethodChannel(binaryMessenger, GlobalConfig.METHOD_CHANNEL_NAME_CAMERA_PLATFORM_VIEW)

    private lateinit var mWakeLock: PowerManager.WakeLock;
    private lateinit var mPowerManager: PowerManager;

    /** Detects, characterizes, and connects to a CameraDevice (used for all camera operations) */
    private val cameraManager: CameraManager by lazy {
        val context = mContext.applicationContext
        context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    }

    /** [CameraCharacteristics] corresponding to the provided Camera ID */
    private lateinit var characteristics: CameraCharacteristics;

    /** Readers used as buffers for camera still shots */
    private lateinit var imageReader: ImageReader

    /** [HandlerThread] where all camera operations run */
    private val cameraThread = HandlerThread("CameraThread").apply { start() }

    /** [Handler] corresponding to [cameraThread] */
    private val cameraHandler = Handler(cameraThread.looper)

    /** Performs recording animation of flashing screen */
    private val animationTask: Runnable by lazy {
        Runnable {
            // Flash white animation
            overlay.background = ColorDrawable(Color.argb(150, 255, 255, 255))
            // Wait for ANIMATION_FAST_MILLIS
            overlay.postDelayed({
                // Remove white flash animation
                overlay.background = null
            }, 50L)
        }
    }

    /** [HandlerThread] where all buffer reading operations run */
    private val imageReaderThread = HandlerThread("imageReaderThread").apply { start() }

    /** [Handler] corresponding to [imageReaderThread] */
    private val imageReaderHandler = Handler(imageReaderThread.looper)

    /** Where the camera preview is displayed */
    private lateinit var viewFinder: AutoFitSurfaceView

    /** Overlay on top of the camera preview */
    private lateinit var overlay: View

    /** The [CameraDevice] that will be opened in this fragment */
    private lateinit var camera: CameraDevice

    /** Internal reference to the ongoing [CameraCaptureSession] configured with our parameters */
    private lateinit var session: CameraCaptureSession

    /** Live data listener for changes in the device orientation relative to the camera */
    private lateinit var relativeOrientation: OrientationLiveData

    companion object {
        private val TAG = "AiCameraPlatformView"
        private const val TAG_WAKE_LOCK = "wake_lock_id"

        /** Maximum number of images that will be held in the reader's buffer */
        private const val IMAGE_BUFFER_SIZE: Int = 3

        /** Maximum time allowed to wait for the result of an image capture */
        private const val IMAGE_CAPTURE_TIMEOUT_MILLIS: Long = 5000

        /** Helper data class used to hold capture metadata with their associated image */
        data class CombinedCaptureResult(
                val image: Image,
                val metadata: CaptureResult,
                val orientation: Int,
                val format: Int
        ) : Closeable {
            override fun close() = image.close()
        }

        /**
         * Create a [File] named a using formatted timestamp with the current date and time.
         *
         * @return [File] created.
         */
        private fun createFile(context: Context, extension: String): File {
            val sdf = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.US)
            return File(context.filesDir, "IMG_${sdf.format(Date())}.$extension")
        }
    }

    init {
        //MethodCallHandler
        mMethodChannel.setMethodCallHandler(this);

        mRootView = LayoutInflater.from(mContext).inflate(R.layout.fragment_camera, null, false);

        overlay = mRootView.findViewById(R.id.overlay)
        viewFinder = mRootView.findViewById(R.id.view_finder)

        mPowerManager = mContext.getSystemService(Context.POWER_SERVICE) as PowerManager;
        
    }


    override fun getView(): View {
        return mRootView;
    }

    override fun dispose() {

    }

    /** MethodCall */
    override fun onMethodCall(call: MethodCall, result: MethodChannel.Result) {
        val method: String = call.method;

        when (method) {
            "startCamera" -> {
                val cameraId: String = call.argument<String>("cameraId")!!;
                val pixelFormat: Int = call.argument<Int>("pixelFormat")!!;

                characteristics = cameraManager.getCameraCharacteristics(cameraId)
                startCamera(cameraId, pixelFormat);

            }
            "takePhoto" -> {
                takePhotoByClick();
            }
            "stopCamera" -> {
                stopCamera();
            }
            "destroyCamera" -> {
                destroyCamera();
            }
        }

    }


    /**
     * InitCamera
     */
    @SuppressLint("InvalidWakeLockTag")
    private fun startCamera(cameraId: String, pixelFormat: Int) {
        mWakeLock = mPowerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, TAG_WAKE_LOCK);
        mWakeLock.acquire();
        
        viewFinder.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceDestroyed(holder: SurfaceHolder) = Unit

            override fun surfaceChanged(
                    holder: SurfaceHolder,
                    format: Int,
                    width: Int,
                    height: Int) = Unit

            override fun surfaceCreated(holder: SurfaceHolder) {

                // Selects appropriate preview size and configures view finder
                val previewSize = getPreviewOutputSize(
                        viewFinder.display, characteristics, SurfaceHolder::class.java)
                Log.d(TAG, "View finder size: ${viewFinder.width} x ${viewFinder.height}")
                Log.d(TAG, "Selected preview size: $previewSize")
                viewFinder.setAspectRatio(previewSize.width, previewSize.height)

                // To ensure that size is set, initialize camera in the view's thread
                view.post { initializeCamera(cameraId = cameraId, pixelFormat = pixelFormat) }
            }
        })

        // Used to rotate the output media to match device orientation
        relativeOrientation = OrientationLiveData(mContext, characteristics).apply {
            observe(this@AiCameraPlatformView, Observer { orientation ->
                Log.d(TAG, "Orientation changed: $orientation")
            })
        }
    }


    /**
     * Begin all camera operations in a coroutine in the main thread. This function:
     * - Opens the camera
     * - Configures the camera session
     * - Starts the preview by dispatching a repeating capture request
     * - Sets up the still image capture listeners
     */
    private fun initializeCamera(cameraId: String, pixelFormat: Int) = lifecycleScope.launch(Dispatchers.Main) {
        // Open the selected camera
        camera = openCamera(cameraManager, cameraId, cameraHandler)

        // Initialize an image reader which will be used to capture still photos
        val size = characteristics.get(
                CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)!!
                .getOutputSizes(pixelFormat).maxBy { it.height * it.width }!!
        imageReader = ImageReader.newInstance(
                size.width, size.height, pixelFormat, IMAGE_BUFFER_SIZE)
        // Creates list of Surfaces where the camera will output frames
        val targets = listOf(viewFinder.holder.surface, imageReader.surface)

        // Start a capture session using our open camera and list of Surfaces where frames will go
        session = createCaptureSession(camera, targets, cameraHandler)

        val captureRequest = camera.createCaptureRequest(
                CameraDevice.TEMPLATE_PREVIEW).apply { addTarget(viewFinder.holder.surface) }

        // This will keep sending the capture request as frequently as possible until the
        // session is torn down or session.stopRepeating() is called
        session.setRepeatingRequest(captureRequest.build(), null, cameraHandler)


    }

    /** Opens the camera and returns the opened device (as the result of the suspend coroutine) */
    @SuppressLint("MissingPermission")
    private suspend fun openCamera(
            manager: CameraManager,
            cameraId: String,
            handler: Handler? = null
    ): CameraDevice = suspendCancellableCoroutine { cont ->
        manager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(device: CameraDevice) = cont.resume(device)

            override fun onDisconnected(device: CameraDevice) {
                Log.w(TAG, "Camera $cameraId has been disconnected")
//                requireActivity().finish()
            }

            override fun onError(device: CameraDevice, error: Int) {
                val msg = when (error) {
                    ERROR_CAMERA_DEVICE -> "Fatal (device)"
                    ERROR_CAMERA_DISABLED -> "Device policy"
                    ERROR_CAMERA_IN_USE -> "Camera in use"
                    ERROR_CAMERA_SERVICE -> "Fatal (service)"
                    ERROR_MAX_CAMERAS_IN_USE -> "Maximum cameras in use"
                    else -> "Unknown"
                }
                val exc = RuntimeException("Camera $cameraId error: ($error) $msg")
                Log.e(TAG, exc.message, exc)
                if (cont.isActive) cont.resumeWithException(exc)
            }
        }, handler)
    }

    /**
     * Starts a [CameraCaptureSession] and returns the configured session (as the result of the
     * suspend coroutine
     */
    private suspend fun createCaptureSession(
            device: CameraDevice,
            targets: List<Surface>,
            handler: Handler? = null
    ): CameraCaptureSession = suspendCoroutine { cont ->

        // Create a capture session using the predefined targets; this also involves defining the
        // session state callback to be notified of when the session is ready
        device.createCaptureSession(targets, object : CameraCaptureSession.StateCallback() {

            override fun onConfigured(session: CameraCaptureSession) = cont.resume(session)

            override fun onConfigureFailed(session: CameraCaptureSession) {
                val exc = RuntimeException("Camera ${device.id} session configuration failed")
                Log.e(TAG, exc.message, exc)
                cont.resumeWithException(exc)
            }
        }, handler)
    }

    /**
     * Helper function used to capture a still image using the [CameraDevice.TEMPLATE_STILL_CAPTURE]
     * template. It performs synchronization between the [CaptureResult] and the [Image] resulting
     * from the single capture, and outputs a [CombinedCaptureResult] object.
     */
    private suspend fun takePhoto():
            CombinedCaptureResult = suspendCoroutine { cont ->

        // Flush any images left in the image reader
        @Suppress("ControlFlowWithEmptyBody")
        while (imageReader.acquireNextImage() != null) {
        }

        // Start a new image queue
        val imageQueue = ArrayBlockingQueue<Image>(IMAGE_BUFFER_SIZE)
        imageReader.setOnImageAvailableListener({ reader ->
            val image = reader.acquireNextImage()
            Log.d(TAG, "Image available in queue: ${image.timestamp}")
            imageQueue.add(image)
        }, imageReaderHandler)

        val captureRequest = session.device.createCaptureRequest(
                CameraDevice.TEMPLATE_STILL_CAPTURE).apply { addTarget(imageReader.surface) }
        session.capture(captureRequest.build(), object : CameraCaptureSession.CaptureCallback() {

            override fun onCaptureStarted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    timestamp: Long,
                    frameNumber: Long) {
                super.onCaptureStarted(session, request, timestamp, frameNumber)
                viewFinder.post(animationTask)
            }

            override fun onCaptureCompleted(
                    session: CameraCaptureSession,
                    request: CaptureRequest,
                    result: TotalCaptureResult) {
                super.onCaptureCompleted(session, request, result)
                val resultTimestamp = result.get(CaptureResult.SENSOR_TIMESTAMP)
                Log.d(TAG, "Capture result received: $resultTimestamp")

                // Set a timeout in case image captured is dropped from the pipeline
                val exc = TimeoutException("Image dequeuing took too long")
                val timeoutRunnable = Runnable { cont.resumeWithException(exc) }
                imageReaderHandler.postDelayed(timeoutRunnable, IMAGE_CAPTURE_TIMEOUT_MILLIS)

                // Loop in the coroutine's context until an image with matching timestamp comes
                // We need to launch the coroutine context again because the callback is done in
                //  the handler provided to the `capture` method, not in our coroutine context
                @Suppress("BlockingMethodInNonBlockingContext")
                lifecycleScope.launch(cont.context) {
                    while (true) {

                        // Dequeue images while timestamps don't match
                        val image = imageQueue.take()
                        // TODO(owahltinez): b/142011420
                        // if (image.timestamp != resultTimestamp) continue
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                                image.format != ImageFormat.DEPTH_JPEG &&
                                image.timestamp != resultTimestamp) continue
                        Log.d(TAG, "Matching image dequeued: ${image.timestamp}")

                        // Unset the image reader listener
                        imageReaderHandler.removeCallbacks(timeoutRunnable)
                        imageReader.setOnImageAvailableListener(null, null)

                        // Clear the queue of images, if there are left
                        while (imageQueue.size > 0) {
                            imageQueue.take().close()
                        }

                        // Compute EXIF orientation metadata
                        val rotation = relativeOrientation.value ?: 0
                        val mirrored = characteristics.get(CameraCharacteristics.LENS_FACING) ==
                                CameraCharacteristics.LENS_FACING_FRONT
                        val exifOrientation = computeExifOrientation(rotation, mirrored)

                        // Build the result and resume progress
                        cont.resume(CombinedCaptureResult(
                                image, result, exifOrientation, imageReader.imageFormat))

                        // There is no need to break out of the loop, this coroutine will suspend
                    }
                }
            }
        }, cameraHandler)
    }

    /** Helper function used to save a [CombinedCaptureResult] into a [File] */
    private suspend fun saveResult(result: CombinedCaptureResult): File = suspendCoroutine { cont ->
        when (result.format) {

            // When the format is JPEG or DEPTH JPEG we can simply save the bytes as-is
            ImageFormat.JPEG, ImageFormat.DEPTH_JPEG -> {
                val buffer = result.image.planes[0].buffer
                val bytes = ByteArray(buffer.remaining()).apply { buffer.get(this) }
                try {
                    val output = createFile(mContext, "jpg")
                    FileOutputStream(output).use { it.write(bytes) }
                    cont.resume(output)
                } catch (exc: IOException) {
                    Log.e(TAG, "Unable to write JPEG image to file", exc)
                    cont.resumeWithException(exc)
                }
            }

            // When the format is RAW we use the DngCreator utility library
            ImageFormat.RAW_SENSOR -> {
                val dngCreator = DngCreator(characteristics, result.metadata)
                try {
                    val output = createFile(mContext, "dng")
                    FileOutputStream(output).use { dngCreator.writeImage(it, result.image) }
                    cont.resume(output)
                } catch (exc: IOException) {
                    Log.e(TAG, "Unable to write DNG image to file", exc)
                    cont.resumeWithException(exc)
                }
            }

            // No other formats are supported by this sample
            else -> {
                val exc = RuntimeException("Unknown image format: ${result.image.format}")
                Log.e(TAG, exc.message, exc)
                cont.resumeWithException(exc)
            }
        }
    }

    private fun stopCamera() {
        try {
            camera.close()
        } catch (exc: Throwable) {
            Log.e(TAG, "Error closing camera", exc)
        }
    }

    private fun destroyCamera() {
        mWakeLock.release();
        cameraThread.quitSafely()
        imageReaderThread.quitSafely()
    }


    private fun takePhotoByClick() = lifecycleScope.launch(Dispatchers.Main) {
        // Listen to the capture button

        // Perform I/O heavy operations in a different scope
        lifecycleScope.launch(Dispatchers.IO) {
            takePhoto().use { result ->
                Log.d(TAG, "Result received: $result")

                // Save the result to disk
                val output = saveResult(result)
                Log.d(TAG, "Image saved: ${output.absolutePath}")

                // If the result is a JPEG file, update EXIF metadata with orientation info
                if (output.extension == "jpg") {
                    val exif = ExifInterface(output.absolutePath)
                    exif.setAttribute(
                            ExifInterface.TAG_ORIENTATION, result.orientation.toString())
                    exif.saveAttributes()
                    Log.d(TAG, "EXIF metadata saved: ${output.absolutePath}")
                }

                // Display the photo taken to user
                lifecycleScope.launch(Dispatchers.Main) {
//                    navController.navigate(CameraFragmentDirections
//                            .actionCameraToJpegViewer(output.absolutePath)
//                            .setOrientation(result.orientation)
//                            .setDepth(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
//                                    result.format == ImageFormat.DEPTH_JPEG))
                }
            }

            // Re-enable click listener after photo is taken
        }
    }

    override fun getLifecycle(): Lifecycle {
        return Lifecycle2();
    }
}

class Lifecycle2 : Lifecycle() {
    override fun addObserver(observer: LifecycleObserver) {

    }

    override fun removeObserver(observer: LifecycleObserver) {
    }

    override fun getCurrentState(): State {
        return State.STARTED
    }

}

