import 'package:ai_camera_example/camera_page.dart';
import 'package:flutter/material.dart';
import 'dart:async';

import 'package:flutter/services.dart';
import 'package:ai_camera/ai_camera.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  String _platformVersion = 'Unknown';

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
            print("点击了：$result");
            goto(result);
          })),
        ],
      ),
    );
  }

  goto(AiCameraSelectorResult result) {
    print("跳转了：$result");
    Navigator.push(context, MaterialPageRoute(builder: (context) {
      return CameraPage(
        cameraId: result.cameraId,
        pixelFormat: result.format,
      );
    }));
  }
}
