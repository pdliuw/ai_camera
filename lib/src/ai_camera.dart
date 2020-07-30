import 'package:ai_camera/src/global_config.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

///
/// AiCameraPlatformWidget
class AiCameraPlatformWidget extends StatefulWidget {
  AiCameraController _controller;

  ///
  /// Default style
  AiCameraPlatformWidget.defaultStyle(
      {@required AiCameraController controller}) {
    _controller = controller;
  }
  @override
  State<StatefulWidget> createState() {
    return _State();
  }
}

class _State extends State<AiCameraPlatformWidget> {
  @override
  Widget build(BuildContext context) {
    return _getCameraWidget();
  }

  Widget _getCameraWidget() {
    TargetPlatform targetPlatform = Theme.of(context).platform;

    if (TargetPlatform.android == targetPlatform) {
      return AndroidView(
        viewType: GlobalConfig.VIEW_TYPE_ID_CAMERA_PLATFORM_VIEW,
        creationParams: {},
        creationParamsCodec: StandardMessageCodec(),
        onPlatformViewCreated: (int id) {
          widget._controller.platformViewCreatedCallback(id);
        },
      );
    } else if (TargetPlatform.iOS == targetPlatform) {
      return UiKitView(
        viewType: GlobalConfig.VIEW_TYPE_ID_CAMERA_PLATFORM_VIEW,
        creationParams: {},
        creationParamsCodec: StandardMessageCodec(),
        onPlatformViewCreated: (int id) {
          widget._controller.platformViewCreatedCallback(id);
        },
      );
    } else {
      return Center(
        child: Text("UnSupported platform"),
      );
    }
  }
}

///
/// AiCameraController
class AiCameraController {
  static const MethodChannel _channel = const MethodChannel(
      GlobalConfig.METHOD_CHANNEL_NAME_CAMERA_PLATFORM_VIEW);
  PlatformViewCreatedCallback _platformViewCreatedCallback;
  AiCameraController({
    @required PlatformViewCreatedCallback platformViewCreatedCallback,
  }) {
    _platformViewCreatedCallback = platformViewCreatedCallback ?? (int id) {};

    //set method handler
    _channel.setMethodCallHandler((MethodCall call) {
      var method = call.method;
      var arguments = call.arguments;

      switch (method) {
        case "ai_camera_result":
          break;

        default:
          break;
      }

      return Future.delayed(Duration(seconds: 1));
    });
  }

  startCamera({
    @required String cameraId,
    @required int pixelFormat,
  }) {
    _channel.invokeMethod("startCamera", {
      "cameraId": cameraId,
      "pixelFormat": pixelFormat,
    });
  }

  takePhoto() {
    _channel.invokeMethod(
      "takePhoto",
    );
  }

  stopCamera() {
    _channel.invokeMethod("stopCamera");
  }

  destroyCamera() {
    _channel.invokeMethod("destroyCamera");
  }

  PlatformViewCreatedCallback get platformViewCreatedCallback =>
      _platformViewCreatedCallback;
}
