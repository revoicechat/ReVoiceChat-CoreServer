package fr.revoicechat.nls;

import java.util.Locale;

import fr.revoicechat.nls.http.CurrentRequestHolder;
import fr.revoicechat.nls.utils.TranslationUtils;

public interface LocalizedMessage {

  String name();

  default String fileName() {
    return getClass().getCanonicalName();
  }

  default String translate(Locale locale, Object... args) {
    return TranslationUtils.translate(this, locale, args);
  }

  default String translate(Object... args) {
    return translate(CurrentRequestHolder.getLocale(), args);
  }
}
