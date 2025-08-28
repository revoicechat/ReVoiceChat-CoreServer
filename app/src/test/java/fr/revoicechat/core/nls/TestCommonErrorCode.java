package fr.revoicechat.core.nls;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class TestCommonErrorCode {

  @ParameterizedTest
  @EnumSource
  void test(CommonErrorCode code) {
    assertThat(code.translate()).isNotEqualTo(code.name());
  }
}