package fr.revoicechat.nls;

import static fr.revoicechat.nls.LocalizedMessageTestEnum.*;
import static org.assertj.core.api.Assertions.*;

import java.util.Locale;

import org.junit.jupiter.api.Test;

class TestLocalizedMessage {

  @Test
  void testEnglishOnlyMessage() {
    assertThat(TEST_IN_ENGLISH_ONLY.translate()).isEqualTo("English");
    assertThat(TEST_IN_ENGLISH_ONLY.translate(Locale.CHINESE)).isEqualTo("English");
    assertThat(TEST_IN_ENGLISH_ONLY.translate(Locale.FRENCH)).isEqualTo("English");
  }

  @Test
  void testFrenchOnlyMessage() {
    assertThat(TEST_IN_FRENCH_ONLY.translate()).isEqualTo("TEST_IN_FRENCH_ONLY");
    assertThat(TEST_IN_FRENCH_ONLY.translate(Locale.CHINESE)).isEqualTo("TEST_IN_FRENCH_ONLY");
    assertThat(TEST_IN_FRENCH_ONLY.translate(Locale.FRENCH)).isEqualTo("Français");
  }

  @Test
  void testFrenchAndEnglishMessage() {
    assertThat(TEST_IN_FRENCH_AND_ENGLISH.translate()).isEqualTo("French and english");
    assertThat(TEST_IN_FRENCH_AND_ENGLISH.translate(Locale.CHINESE)).isEqualTo("French and english");
    assertThat(TEST_IN_FRENCH_AND_ENGLISH.translate(Locale.FRENCH)).isEqualTo("Français et anglais");
  }

  @Test
  void testWithNoTranslation() {
    assertThat(TEST_WITH_NO_TRANSLATION.translate()).isEqualTo("TEST_WITH_NO_TRANSLATION");
    assertThat(TEST_WITH_NO_TRANSLATION.translate(Locale.CHINESE)).isEqualTo("TEST_WITH_NO_TRANSLATION");
    assertThat(TEST_WITH_NO_TRANSLATION.translate(Locale.FRENCH)).isEqualTo("TEST_WITH_NO_TRANSLATION");
  }

  @Test
  void testWithNoTranslationFile() {
    assertThat(NotTranslatedTestEnum.TEST.translate()).isEqualTo("TEST");
    assertThat(NotTranslatedTestEnum.TEST.translate(Locale.CHINESE)).isEqualTo("TEST");
    assertThat(NotTranslatedTestEnum.TEST.translate(Locale.FRENCH)).isEqualTo("TEST");
  }
}