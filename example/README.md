# ai_camera_example

<details>
* main

<summary>main</summary>

```

import 'package:ai_camera/ai_camera.dart';
import 'package:ai_camera_example/camera_list_widget.dart';
import 'package:ai_camera_example/camera_page.dart';
import 'package:flutter/material.dart';
import 'package:permission_handler/permission_handler.dart';

import 'ai_camera_platform_selector_widget.dart';

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

class _AppState extends State<App> with SingleTickerProviderStateMixin {
  List<String> _list = [
    "Platform Selector",
    "Flutter Selector",
  ];

  TabController _tabController;
  @override
  void initState() {
    super.initState();

    _tabController = TabController(length: _list.length, vsync: this);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text('Plugin example app'),
        bottom: TabBar(
            controller: _tabController,
            tabs: _list
                .map((e) => Tab(
                      text: e,
                    ))
                .toList()),
      ),
      body: TabBarView(
        children: [
          AiCameraPlatformSelectWidget(),
          CameraListWidget(),
        ],
        controller: _tabController,
      ),
    );
  }
}


```

</details>
<details>
* camera_list_widget

<summary>camera_list_widget</summary>

```

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


```

</details>
<details>
* camera

<summary>camera</summary>

```

import 'package:ai_camera/ai_camera.dart';
import 'package:flutter/material.dart';

///
/// CameraPage
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


```

</details>