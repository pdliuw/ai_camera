import 'package:ai_camera/ai_camera.dart';
import 'package:ai_camera/src/global_config.dart';
import 'package:flutter/services.dart';

/// [AiCameraSelectorCameraListCallback]
typedef AiCameraSelectorCameraListCallback = void Function(
    List<AiCameraSelectorResult> result);

///
/// AiCameraSelector
/// * Get Camera list [getCameraList]
/// * Receive Camera list [addSelectorCameraListCallback]\[AiCameraSelectorCameraListCallback]
class AiCameraSelector {
  ///MethodChannel
  MethodChannel _methodChannel =
      MethodChannel(GlobalConfig.METHOD_CHANNEL_NAME_CAMERA_SELECTOR);

  ///Singleton
  static AiCameraSelector instance = AiCameraSelector._();

  ///Camera list container
  List<AiCameraSelectorResult> _cameraList = [];

  ///CameraCallback
  List<AiCameraSelectorCameraListCallback> _cameraListCallbackList = [];

  ///
  /// Instance
  AiCameraSelector._() {
    _methodChannel.setMethodCallHandler(_handler);
  }

  ///
  /// Handler method
  Future<dynamic> _handler(
    MethodCall call,
  ) {
    String method = call.method;
    switch (method) {
      case "ai_camera_camera_list_result_start":
        _cameraList.clear();
        break;

      case "ai_camera_camera_list_result":
        var camera = AiCameraSelectorResult.defaultStyle(
          arguments: call.arguments,
        );

        _cameraList.add(camera);
        break;

      case "ai_camera_camera_list_result_end":
        for (var callback in _cameraListCallbackList) {
          callback(_cameraList);
        }
        break;
    }

    return Future.delayed(Duration(seconds: 1));
  }

  ///
  /// Add callback
  /// [removeSelectorCameraListCallback]
  addSelectorCameraListCallback({
    AiCameraSelectorCameraListCallback cameraListCallback,
  }) {
    _cameraListCallbackList.add(cameraListCallback);
  }

  ///
  /// Remove callback
  /// [addSelectorCameraListCallback]
  removeSelectorCameraListCallback({
    AiCameraSelectorCameraListCallback cameraListCallback,
  }) {
    _cameraListCallbackList.remove(cameraListCallback);
  }

  ///
  /// Get camera list
  getCameraList() {
    _methodChannel.invokeMethod("cameraList", {});
  }
}
