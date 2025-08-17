package fr.revoicechat.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.MediaType.*;

import java.io.IOException;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.databind.ObjectMapper;

class TestContentAwareAccessDeniedHandler {


  public static Stream<Arguments> data() {
    return Stream.of(
        Arguments.of(RequestMethod.GET, APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE, true),
        Arguments.of(RequestMethod.GET, APPLICATION_JSON_VALUE, APPLICATION_PDF_VALUE, true),
        Arguments.of(RequestMethod.GET, APPLICATION_JSON_VALUE, "", true),
        Arguments.of(RequestMethod.GET, APPLICATION_JSON_VALUE, null, true),

        Arguments.of(RequestMethod.GET, APPLICATION_PDF_VALUE, APPLICATION_JSON_VALUE, true),
        Arguments.of(RequestMethod.GET, APPLICATION_PDF_VALUE, APPLICATION_PDF_VALUE, false),
        Arguments.of(RequestMethod.GET, APPLICATION_PDF_VALUE, "", false),
        Arguments.of(RequestMethod.GET, APPLICATION_PDF_VALUE, null, false),

        Arguments.of(RequestMethod.GET, "", APPLICATION_JSON_VALUE, true),
        Arguments.of(RequestMethod.GET, "", APPLICATION_PDF_VALUE, false),
        Arguments.of(RequestMethod.GET, "", "", false),
        Arguments.of(RequestMethod.GET, "", null, false),

        Arguments.of(RequestMethod.GET, null, APPLICATION_JSON_VALUE, true),
        Arguments.of(RequestMethod.GET, null, APPLICATION_PDF_VALUE, false),
        Arguments.of(RequestMethod.GET, null, "", false),
        Arguments.of(RequestMethod.GET, null, null, false),

        Arguments.of(RequestMethod.POST, APPLICATION_JSON_VALUE, APPLICATION_JSON_VALUE, true),
        Arguments.of(RequestMethod.POST, APPLICATION_JSON_VALUE, APPLICATION_PDF_VALUE, true),
        Arguments.of(RequestMethod.POST, APPLICATION_JSON_VALUE, "", true),
        Arguments.of(RequestMethod.POST, APPLICATION_JSON_VALUE, null, true),

        Arguments.of(RequestMethod.POST, APPLICATION_PDF_VALUE, APPLICATION_JSON_VALUE, true),
        Arguments.of(RequestMethod.POST, APPLICATION_PDF_VALUE, APPLICATION_PDF_VALUE, true),
        Arguments.of(RequestMethod.POST, APPLICATION_PDF_VALUE, "", true),
        Arguments.of(RequestMethod.POST, APPLICATION_PDF_VALUE, null, true),

        Arguments.of(RequestMethod.POST, "", APPLICATION_JSON_VALUE, true),
        Arguments.of(RequestMethod.POST, "", APPLICATION_PDF_VALUE, true),
        Arguments.of(RequestMethod.POST, "", "", true),
        Arguments.of(RequestMethod.POST, "", null, true),

        Arguments.of(RequestMethod.POST, null, APPLICATION_JSON_VALUE, true),
        Arguments.of(RequestMethod.POST, null, APPLICATION_PDF_VALUE, true),
        Arguments.of(RequestMethod.POST, null, "", true),
        Arguments.of(RequestMethod.POST, null, null, true)
    );
  }

  @ParameterizedTest
  @MethodSource("data")
  void test(RequestMethod method, String accept, String content, boolean isJson) throws IOException {
    // Given
    var request = new MockHttpServletRequest(method.name(), "url");
    Optional.ofNullable(accept).ifPresent(a -> request.addHeader("Accept", a));
    Optional.ofNullable(content).ifPresent(a -> request.addHeader("Content-Type", a));
    var response = new MockHttpServletResponse();
    // When
    new ContentAwareAccessDeniedHandler().handle(request, response, 403);
    // Then
    assertThat(response.getStatus()).isEqualTo(403);
    assertThat(isJson(response.getContentAsString())).isEqualTo(isJson);
    if (isJson) {
      assertThat(response.getContentType()).isEqualTo(APPLICATION_JSON_VALUE);
    } else {
      assertThat(response.getContentType()).isEqualTo(TEXT_HTML_VALUE);
    }
  }

  private boolean isJson(String input) {
    try {
      new ObjectMapper().readTree(input);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

}