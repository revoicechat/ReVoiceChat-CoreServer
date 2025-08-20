package fr.revoicechat.nls.utils;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Objects;
import java.util.ResourceBundle;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import fr.revoicechat.nls.LocalizedMessage;
import jakarta.servlet.http.HttpServletRequest;

public class TranslationUtils {

  private TranslationUtils() {/*Cannot instanciate*/}

  private static final Locale DEFAULT_LOCALE = Locale.ENGLISH;

  public static Locale getCurrentRequestLocale() {
    RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
    if (requestAttributes instanceof ServletRequestAttributes servletRequestAttributes) {
      HttpServletRequest request = servletRequestAttributes.getRequest();
      return request.getLocale(); // or request.getLocales() for all accepted locales
    }
    return TranslationUtils.DEFAULT_LOCALE; // fallback if no request bound
  }

  public static String translate(LocalizedMessage message, Locale locale, Object... args) {
    try {
      ResourceBundle bundle = ResourceBundle.getBundle(message.fileName(), locale, new FallbackToEnglishControl());
      String pattern = bundle.getString(message.name());
      return MessageFormat.format(pattern, args);
    } catch (MissingResourceException e) {
      if (!Objects.equals(locale, DEFAULT_LOCALE)) {
        return translate(message, DEFAULT_LOCALE, args);
      }
      return message.name();
    }
  }

  private static class FallbackToEnglishControl extends ResourceBundle.Control {
    @Override
    public Locale getFallbackLocale(String baseName, Locale locale) {
      return DEFAULT_LOCALE.equals(locale) ? null : DEFAULT_LOCALE;
    }
  }
}
