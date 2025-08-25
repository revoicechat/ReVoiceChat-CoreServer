package fr.revoicechat.nls;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TestUserErrorCode {

  @ParameterizedTest
  @EnumSource
  void test(UserErrorCode code) {
    assertThat(code.translate()).isNotEqualTo(code.name());
  }
}