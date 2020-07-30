import 'package:ai_camera/src/global_config.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

///
/// AiCameraPlatformWidget
class AiCameraPlatformWidget extends StatefulWidget {
  @override
  State<StatefulWidget> createState() {
    return _State();
  }
}

class _State extends State<AiCameraPlatformWidget> {
  static const MethodChannel _channel = const MethodChannel(
      GlobalConfig.METHOD_CHANNEL_NAME_CAMERA_PLATFORM_VIEW);

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
        onPlatformViewCreated: (int id) {},
      );
    } else if (TargetPlatform.iOS == targetPlatform) {
      return UiKitView(
        viewType: GlobalConfig.VIEW_TYPE_ID_CAMERA_PLATFORM_VIEW,
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
