import 'package:ai_camera/src/ai_camera_selector_result.dart';
import 'package:ai_camera/src/global_config.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

/// [AiCameraSelectorCallback]
typedef AiCameraSelectorCallback = Function(AiCameraSelectorResult result);

///
/// CameraSelector PlatformWidget
/// [AiCameraSelectorPlatformWidget]
/// [GlobalConfig]
/// [AiCameraSelectorCallback]
class AiCameraSelectorPlatformWidget extends StatefulWidget {
  AiCameraSelectorCallback _aiCameraSelectorCallback;
  AiCameraSelectorPlatformWidget.defaultStyle({
    @required AiCameraSelectorCallback selectorCallback,
  }) {
    //Must not be null
    _aiCameraSelectorCallback = selectorCallback ??
        (AiCameraSelectorResult result) {
          //do nothing
        };
  }
  @override
  State<StatefulWidget> createState() {
    return _State();
  }
}

class _State extends State<AiCameraSelectorPlatformWidget> {
  static const MethodChannel _channel = const MethodChannel(
      GlobalConfig.METHOD_CHANNEL_NAME_CAMERA_SELECTOR_PLATFORM_VIEW);

  @override
  void initState() {
    super.initState();

    _channel.setMethodCallHandler((MethodCall call) {
      var method = call.method;
      var arguments = call.arguments;

      switch (method) {
        case "ai_camera_selector_result":
          var selectorResult =
              AiCameraSelectorResult.defaultStyle(arguments: arguments);

          widget._aiCameraSelectorCallback(selectorResult);
          break;

        default:
          break;
      }

      return Future.delayed(Duration(seconds: 1));
    });
  }

  @override
  Widget build(BuildContext context) {
    return _getCameraWidget();
  }

  Widget _getCameraWidget() {
    TargetPlatform targetPlatform = Theme.of(context).platform;

    if (TargetPlatform.android == targetPlatform) {
      return AndroidView(
        viewType: GlobalConfig.VIEW_TYPE_ID_CAMERA_SELECTOR_PLATFORM_VIEW,
        creationParams: {},
        creationParamsCodec: StandardMessageCodec(),
        onPlatformViewCreated: (int id) {},
      );
    } else if (TargetPlatform.iOS == targetPlatform) {
      return UiKitView(
        viewType: GlobalConfig.VIEW_TYPE_ID_CAMERA_SELECTOR_PLATFORM_VIEW,
        creationParams: {},
        creationParamsCodec: StandardMessageCodec(),
        onPlatformViewCreated: (int id) {},
      );
    } else {
      return Center(
        child: Text("UnSupported platform"),
      );
    }
  }
}
