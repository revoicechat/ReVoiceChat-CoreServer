package fr.revoicechat.nls;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TestHttpStatusErrorCode {

  @ParameterizedTest
  @EnumSource
  void test(HttpStatusErrorCode code) {
    assertThat(code.translate()).isNotEqualTo(code.name());
  }
}