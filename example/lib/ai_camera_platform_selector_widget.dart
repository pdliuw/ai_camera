import 'package:ai_camera/ai_camera.dart';
import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';

import 'camera_page.dart';

class AiCameraPlatformSelectWidget extends StatefulWidget {
  @override
  _AiCameraPlatformSelectWidgetState createState() =>
      _AiCameraPlatformSelectWidgetState();
}

class _AiCameraPlatformSelectWidgetState
    extends State<AiCameraPlatformSelectWidget>
    with AutomaticKeepAliveClientMixin {
  @override
  Widget build(BuildContext context) {
    super.build(context);
    return Column(
      children: <Widget>[
        Expanded(child: AiCameraSelectorPlatformWidget.defaultStyle(
            selectorCallback: (AiCameraSelectorResult result) {
          goto(result);
        })),
      ],
    );
  }

  goto(AiCameraSelectorResult result) async {
    if (await Permission.camera.request().isGranted) {
      Navigator.push(context, MaterialPageRoute(builder: (context) {
        return CameraPage(
          cameraId: result.cameraId,
          pixelFormat: result.format,
        );
      }));
    }
  }

  @override
  bool get wantKeepAlive => true;
}
