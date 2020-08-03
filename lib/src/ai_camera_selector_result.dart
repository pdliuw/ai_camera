///
/// AiCameraSelectorResult receiver model
class AiCameraSelectorResult {
  String _title;
  String _cameraId;
  int _format;

  AiCameraSelectorResult.defaultStyle({
    dynamic arguments,
  }) {
    _title = arguments['title'];
    _cameraId = arguments['cameraId'];
    _format = arguments['format'];
  }
  String get title => _title;
  String get cameraId => _cameraId;
  int get format => _format;

  @override
  String toString() {
    return """
    
    AiCameraSelectorResult:
      title:$title,
      cameraId:$cameraId,
      format:$format,
      
    """;
  }
}
