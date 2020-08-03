import 'package:ai_camera/ai_camera.dart';
import 'package:ai_camera_example/camera_page.dart';
import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';

///
/// CameraListWidget
///
class CameraListWidget extends StatefulWidget {
  @override
  _CameraListWidgetState createState() => _CameraListWidgetState();
}

class _CameraListWidgetState extends State<CameraListWidget>
    with AutomaticKeepAliveClientMixin {
  List<AiCameraSelectorResult> _list = [];
  @override
  void initState() {
    super.initState();
    //add callback
    AiCameraSelector.instance.addSelectorCameraListCallback(
        cameraListCallback: (cameraList) {
      setState(() {
        _list = cameraList;
      });
    });
    //get cameras
    AiCameraSelector.instance.getCameraList();
  }

  @override
  Widget build(BuildContext context) {
    super.build(context);
    return Column(
      children: _list
          .map(
            (e) => ListTile(
              onTap: () {
                goto(e);
              },
              title: Text("${e.title}"),
              subtitle: Text("${e.cameraId}"),
            ),
          )
          .toList(),
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
