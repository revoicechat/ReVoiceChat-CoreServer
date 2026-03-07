package fr.revoicechat.opengraph.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class TestOpenGraphService {

  @Test
  void testWithOneUrl() {
    // Given
    var text = """
        look https://github.com/revoicechat/ReVoiceChat-CoreServer for
        the core part of ReVoiceChat""";
    // When
    var result = new OpenGraphService().extract(text);
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
    assertThat(new OpenGraphService().extract("""
        - https://github.com/revoicechat/ReVoiceChat-CoreServer
        - https://github.com/revoicechat/revoicechat""")
    ).isNull();
  }

  @Test
  void testWithNoUrl() {
    assertThat(new OpenGraphService().extract("no url")).isNull();
  }

  @Test
  void testWithNull() {
    assertThat(new OpenGraphService().extract(null)).isNull();
  }
}