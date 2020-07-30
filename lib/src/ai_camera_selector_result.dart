///
/// AiCameraSelectorResult receiver model
class AiCameraSelectorResult {
  String _cameraId;
  int _format;

  AiCameraSelectorResult.defaultStyle({
    dynamic arguments,
  }) {
    _cameraId = arguments['cameraId'];
    _format = arguments['format'];
  }

  String get cameraId => _cameraId;
  int get format => _format;

  @override
  String toString() {
    return """
    
    AiCameraSelectorResult:
      cameraId:$cameraId,
      format:$format,
      
    """;
  }
}
