import 'dart:async';

import 'package:flutter/services.dart';

class AiCamera {
  static const MethodChannel _channel =
      const MethodChannel('ai_camera');

  static Future<String> get platformVersion async {
    final String version = await _channel.invokeMethod('getPlatformVersion');
    return version;
  }
}
