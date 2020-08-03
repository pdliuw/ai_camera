# ai_camera

![totem](https://raw.githubusercontent.com/pdliuw/pdliuw.github.io/master/images/totem_four_logo.jpg)

-----

|[English Document](https://github.com/pdliuw/ai_camera/blob/master/README_EN.md)|[中文文档](https://github.com/pdliuw/ai_camera)|
|:-|:-|



## 1.安装

使用当前包作为依赖库

### 1. 依赖此库

在文件 'pubspec.yaml' 中添加

[![pub package](https://img.shields.io/pub/v/ai_camera.svg)](https://pub.dev/packages/ai_camera)

```

dependencies:

  ai_camera: ^version

```

或者以下方式依赖

```
dependencies:

  # ai_camera plugin.
  ai_camera:
    git:
      url: https://github.com/pdliuw/ai_camera.git

```

### 2. 安装此库

你可以通过下面的命令行来安装此库

```

$ flutter pub get


```

你也可以通过项目开发工具通过可视化操作来执行上述步骤

### 3. 导入此库

现在，在你的Dart编辑代码中，你可以使用：

```

import 'package:ai_camera/ai_camera.dart';

```


## 2.使用

使用'地图'需要动态申请权限，动态权限推荐：[permission_handler](https://github.com/Baseflow/flutter-permission-handler)

配置权限

<details>
<summary>Android</summary>

```

    <uses-permission android:name="android.permission.CAMERA"/>
    <uses-permission android:name="android.permission.WAKE_LOCK"/>

```



</details>

<details>
<summary>ios</summary>

```

	<key>NSCameraUsageDescription</key>
	<string>Usage camera</string>

```



iOS支持PlatformView配置：

```
	
    <key>io.flutter.embedded_views_preview</key>
    <true/>
    
```
</details>

* Camera devices

```


    //add callback
    AiCameraSelector.instance.addSelectorCameraListCallback(
        cameraListCallback: (cameraList) {
      setState(() {
        _list = cameraList;
      });
    });
    //get cameras
    AiCameraSelector.instance.getCameraList();

```

* Camera preview

```


      Column(
        children: <Widget>[
          Expanded(
              child:
                  AiCameraPlatformWidget.defaultStyle(controller: _controller))
        ],
      ),

```

BSD 3-Clause License

Copyright (c) 2020, pdliuw
All rights reserved.


