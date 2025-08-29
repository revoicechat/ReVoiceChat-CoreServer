package fr.revoicechat.core.web.error;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;

final class ErrorMapperUtils {
  private ErrorMapperUtils() {}

  static final String JSON_MESSAGE = fetchForbiddenAccessFile("/static/forbidden-access.json");
  static final String HTML_MESSAGE = fetchForbiddenAccessFile("/static/forbidden-access.html");

  private static String fetchForbiddenAccessFile(String name) {
    try (var ressource = GlobalExceptionMapper.class.getResourceAsStream(name)) {
      assert ressource != null;
      return new String(ressource.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  static MediaType determineResponseType(HttpHeaders headers) {
    if (headers != null) {
      List<MediaType> acceptable = headers.getAcceptableMediaTypes();
      for (MediaType mediaType : acceptable) {
        if (mediaType.isCompatible(MediaType.APPLICATION_JSON_TYPE)) {
          return MediaType.APPLICATION_JSON_TYPE;
        } else if (mediaType.isCompatible(MediaType.TEXT_HTML_TYPE)) {
          return MediaType.TEXT_HTML_TYPE;
        }
      }
    }
    return MediaType.TEXT_HTML_TYPE;
  }
}
