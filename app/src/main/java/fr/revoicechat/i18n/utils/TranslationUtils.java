package fr.revoicechat.i18n.utils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import fr.revoicechat.i18n.LocalizedMessage;

public class TranslationUtils {

  private TranslationUtils() {/*not instantiable*/}

  private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

  public static String translate(String fileName, String name, List<Locale> acceptedLanguage, Object... args) {
    for (Locale locale : acceptedLanguage) {
      try {
        ResourceBundle bundle = ResourceBundle.getBundle(fileName, locale, new FallbackToEnglishControl());
        String pattern = bundle.getString(name);
        return MessageFormat.format(pattern, args);
      } catch (MissingResourceException e) {
        // is the first accepted language is not present, we test the next one
      }
    }
    return name;
  }

  public static String translate(LocalizedMessage message, List<Locale> acceptedLanguage, Object... args) {
    return translate(message.fileName(), message.name(), acceptedLanguage, args);
  }

  private static class FallbackToEnglishControl extends ResourceBundle.Control {
    @Override
    public Locale getFallbackLocale(String baseName, Locale locale) {
      return DEFAULT_LOCALE.equals(locale) ? null : DEFAULT_LOCALE;
    }
  }
}
