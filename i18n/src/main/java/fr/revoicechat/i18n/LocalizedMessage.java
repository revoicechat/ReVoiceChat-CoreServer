package fr.revoicechat.i18n;

import java.util.List;
import java.util.Locale;

import fr.revoicechat.i18n.http.CurrentRequestHolder;
import fr.revoicechat.i18n.utils.TranslationUtils;

public interface LocalizedMessage {

  String name();

  default String fileName() {
    return getClass().getCanonicalName();
  }

  default String translate(List<Locale> locale, Object... args) {
    return TranslationUtils.translate(this, locale, args);
  }

  default String translate(Object... args) {
    return translate(CurrentRequestHolder.getLocale(), args);
  }
}
