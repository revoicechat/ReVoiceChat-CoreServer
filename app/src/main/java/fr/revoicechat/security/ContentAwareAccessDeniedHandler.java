package fr.revoicechat.security;

import static org.springframework.http.MediaType.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class ContentAwareAccessDeniedHandler {

  private static final String JSON_MESSAGE = fetchForbiddenAccessFile("/static/forbidden-access.json");
  private static final String HTML_MESSAGE = fetchForbiddenAccessFile("/static/forbidden-access.html");

  private static String fetchForbiddenAccessFile(String name) {
    try (var ressource = ContentAwareAccessDeniedHandler.class.getResourceAsStream(name)) {
      assert ressource != null;
      return new String(ressource.readAllBytes(), StandardCharsets.UTF_8);
    } catch (IOException e) {
      throw new IllegalArgumentException(e);
    }
  }

  void handle(HttpServletRequest request, HttpServletResponse response, int status) throws IOException {
    String accept = request.getHeader("Accept");
    String contentType = request.getHeader("Content-Type");
    String method = request.getMethod();
    boolean wantsJson = isMethodDoesNotHandleHTML(method)
                        || isApplicationJson(accept)
                        || isApplicationJson(contentType);
    response.setStatus(status);
    if (wantsJson) {
      response.setContentType(APPLICATION_JSON_VALUE);
      try (var writer = response.getWriter()) {
        writer.write(JSON_MESSAGE);
      }
    } else {
      response.setContentType(TEXT_HTML_VALUE);
      try (var writer = response.getWriter()) {
        writer.write(HTML_MESSAGE);
      }
    }
  }

  private static boolean isMethodDoesNotHandleHTML(final String method) {
    return !"GET".equalsIgnoreCase(method);
  }

  private static boolean isApplicationJson(final String accept) {
    return accept != null && accept.contains("application/json");
  }
}
