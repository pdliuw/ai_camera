import 'package:ai_camera/ai_camera.dart';
import 'package:flutter/material.dart';

class CameraPage extends StatefulWidget {
  String cameraId;
  int pixelFormat;
  CameraPage({
    this.cameraId,
    this.pixelFormat,
  }) {}
  @override
  _CameraPageState createState() => _CameraPageState();
}

class _CameraPageState extends State<CameraPage> {
  AiCameraController _controller;
  @override
  void initState() {
    super.initState();

    _controller = AiCameraController(platformViewCreatedCallback: (id) {
      _controller.startCamera(
          cameraId: widget.cameraId, pixelFormat: widget.pixelFormat);
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(),
      body: Column(
        children: <Widget>[
          Expanded(
              child:
                  AiCameraPlatformWidget.defaultStyle(controller: _controller))
        ],
      ),
    );
  }

  @override
  void dispose() {
    super.dispose();
    _controller.stopCamera();
    _controller.destroyCamera();
  }
}
