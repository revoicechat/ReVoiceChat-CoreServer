package fr.revoicechat.i18n;

public interface LocalizedMessage {

  String name();

  default String fileName() {
    return getClass().getCanonicalName();
  }

  default String translate(Object... args) {
    return TranslationUtils.translate(this, args);
  }
}
