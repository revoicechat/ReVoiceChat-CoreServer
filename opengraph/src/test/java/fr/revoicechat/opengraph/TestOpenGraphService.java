package fr.revoicechat.opengraph;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;

@QuarkusTest
class TestOpenGraphService {

  @Inject OpenGraphExtractor extractor;

  @Test
  void testWithOneUrl() {
    // Given
    var text = """
        look https://github.com/revoicechat/ReVoiceChat-CoreServer for
        the core part of ReVoiceChat""";
    // When
    var result = extractor.extract(text);
    // Then
    assertThat(result).isNotNull();
    assertThat(result.getBasic().url()).isEqualTo("https://github.com/revoicechat/ReVoiceChat-CoreServer");
    assertThat(result.getBasic().title()).contains("revoicechat/ReVoiceChat-CoreServer");
    assertThat(result.getPage().description()).contains("Backend server of revoicechat");
    assertThat(result.getPage().siteName()).isEqualTo("GitHub");
    assertThat(result.getImage().image()).startsWith("https://opengraph.githubassets.com/");
  }

  @Test
  void testWithTwoUrl() {
    // Given
    var text = """
        - https://github.com/revoicechat/ReVoiceChat-CoreServer
        - https://github.com/revoicechat/revoicechat""";
    // When
    var result = extractor.extract(text);
    // Then
    assertThat(result).isNull();
  }

  @Test
  void testWithNoUrl() {
    // Given
    var text = "no url";
    // When
    var result = extractor.extract(text);
    // Then
    assertThat(result).isNull();
  }
}