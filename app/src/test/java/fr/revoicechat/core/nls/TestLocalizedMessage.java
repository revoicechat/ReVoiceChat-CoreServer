package fr.revoicechat.core.nls;

import static fr.revoicechat.core.nls.LocalizedMessageTestEnum.*;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;

class TestLocalizedMessage {

  @Test
  void testEnglishOnlyMessage() {
    assertThat(TEST_IN_ENGLISH_ONLY.translate()).isEqualTo("English");
    assertThat(TEST_IN_ENGLISH_ONLY.translate(List.of(Locale.CHINESE, Locale.ENGLISH))).isEqualTo("English");
    assertThat(TEST_IN_ENGLISH_ONLY.translate(List.of(Locale.FRENCH, Locale.ENGLISH))).isEqualTo("English");
  }

  @Test
  void testFrenchOnlyMessage() {
    assertThat(TEST_IN_FRENCH_ONLY.translate()).isEqualTo("TEST_IN_FRENCH_ONLY");
    assertThat(TEST_IN_FRENCH_ONLY.translate(List.of(Locale.CHINESE))).isEqualTo("TEST_IN_FRENCH_ONLY");
    assertThat(TEST_IN_FRENCH_ONLY.translate(List.of(Locale.FRENCH))).isEqualTo("Français");
  }

  @Test
  void testFrenchAndEnglishMessage() {
    assertThat(TEST_IN_FRENCH_AND_ENGLISH.translate()).isEqualTo("French and english");
    assertThat(TEST_IN_FRENCH_AND_ENGLISH.translate(List.of(Locale.CHINESE))).isEqualTo("French and english");
    assertThat(TEST_IN_FRENCH_AND_ENGLISH.translate(List.of(Locale.FRENCH))).isEqualTo("Français et anglais");
  }

  @Test
  void testWithNoTranslation() {
    assertThat(TEST_WITH_NO_TRANSLATION.translate()).isEqualTo("TEST_WITH_NO_TRANSLATION");
    assertThat(TEST_WITH_NO_TRANSLATION.translate(List.of(Locale.CHINESE))).isEqualTo("TEST_WITH_NO_TRANSLATION");
    assertThat(TEST_WITH_NO_TRANSLATION.translate(List.of(Locale.FRENCH))).isEqualTo("TEST_WITH_NO_TRANSLATION");
  }

  @Test
  void testWithNoTranslationFile() {
    assertThat(NotTranslatedTestEnum.TEST.translate()).isEqualTo("TEST");
    assertThat(NotTranslatedTestEnum.TEST.translate(List.of(Locale.CHINESE))).isEqualTo("TEST");
    assertThat(NotTranslatedTestEnum.TEST.translate(List.of(Locale.FRENCH))).isEqualTo("TEST");
  }
}