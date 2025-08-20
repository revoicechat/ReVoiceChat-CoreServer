package fr.revoicechat.nls;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

/**
 * Tests associés à {@link ServerErrorCode}.
 */
class TestServerErrorCode {

  @ParameterizedTest
  @EnumSource
  void test(ServerErrorCode code) {
    assertThat(code.translate()).isNotEqualTo(code.name());
  }
}