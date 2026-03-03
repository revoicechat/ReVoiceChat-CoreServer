package fr.revoicechat.opengraph.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;

import org.junit.jupiter.api.Test;

class TestOpenGraphService {

  @Test
  void testWithOneUrl() throws IOException, InterruptedException {
    // Given
    var text = """
        look https://github.com/revoicechat/ReVoiceChat-CoreServer for
        the core part of ReVoiceChat""";
    // When
    var result = new OpenGraphService().extractForSingleUrl(text);
    // Then
    assertThat(result).isNotNull();
    assertThat(result.url()).isEqualTo("https://github.com/revoicechat/ReVoiceChat-CoreServer");
    assertThat(result.title()).contains("revoicechat/ReVoiceChat-CoreServer");
    assertThat(result.description()).contains("Backend server of revoicechat");
    assertThat(result.siteName()).isEqualTo("GitHub");
    assertThat(result.image()).startsWith("https://opengraph.githubassets.com/");
  }

  @Test
  void testWithTwoUrl() throws IOException, InterruptedException {
    // Given
    var text = """
        - https://github.com/revoicechat/ReVoiceChat-CoreServer
        - https://github.com/revoicechat/revoicechat""";
    // When
    var result = new OpenGraphService().extractForSingleUrl(text);
    // Then
    assertThat(result).isNull();
  }

  @Test
  void testWithNoUrl() throws IOException, InterruptedException {
    // Given
    var text = "no url";
    // When
    var result = new OpenGraphService().extractForSingleUrl(text);
    // Then
    assertThat(result).isNull();
  }
}