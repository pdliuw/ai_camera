import 'package:ai_camera/ai_camera.dart';
import 'package:ai_camera_example/camera_page.dart';
import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  @override
  void initState() {
    super.initState();
  }

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: App(),
    );
  }
}

class App extends StatefulWidget {
  @override
  _AppState createState() => _AppState();
}

class _AppState extends State<App> {
  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
      ),
      body: Column(
        children: <Widget>[
          Expanded(child: AiCameraSelectorPlatformWidget.defaultStyle(
              selectorCallback: (AiCameraSelectorResult result) {
            goto(result);
          })),
        ],
      ),
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
}
