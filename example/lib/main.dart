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
