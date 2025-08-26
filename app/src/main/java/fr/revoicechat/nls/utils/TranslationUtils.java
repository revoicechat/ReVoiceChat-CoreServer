package fr.revoicechat.nls.utils;

import java.text.MessageFormat;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import fr.revoicechat.nls.LocalizedMessage;

public class TranslationUtils {

  private TranslationUtils() {/*not instantiable*/}

  private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

  public static String translate(LocalizedMessage message, List<Locale> accepableLanguage, Object... args) {
    for (Locale locale : accepableLanguage) {
      try {
        ResourceBundle bundle = ResourceBundle.getBundle(message.fileName(), locale, new FallbackToEnglishControl());
        String pattern = bundle.getString(message.name());
        return MessageFormat.format(pattern, args);
      } catch (MissingResourceException e) {
        // is the first acceptable language is not present, we test the next
      }
    }
    return message.name();
  }

  private static class FallbackToEnglishControl extends ResourceBundle.Control {
    @Override
    public Locale getFallbackLocale(String baseName, Locale locale) {
      return DEFAULT_LOCALE.equals(locale) ? null : DEFAULT_LOCALE;
    }
  }
}
