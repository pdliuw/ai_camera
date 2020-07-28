#import "AiCameraPlugin.h"
#if __has_include(<ai_camera/ai_camera-Swift.h>)
#import <ai_camera/ai_camera-Swift.h>
#else
// Support project import fallback if the generated compatibility header
// is not copied when this plugin is created as a library.
// https://forums.swift.org/t/swift-static-libraries-dont-copy-generated-objective-c-header/19816
#import "ai_camera-Swift.h"
#endif

@implementation AiCameraPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftAiCameraPlugin registerWithRegistrar:registrar];
}
@end
