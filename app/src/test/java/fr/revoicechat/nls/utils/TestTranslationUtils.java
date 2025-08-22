package fr.revoicechat.nls.utils;

import java.util.List;
import java.util.Locale;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

class TestTranslationUtils {

  @Test
  void testCurrentRequestLocaleNoAttributes() {
    Assertions.assertThat(TranslationUtils.getCurrentRequestLocale()).isEqualTo(Locale.ENGLISH);
  }

  @Test
  void testCurrentRequestLocale() {
    var oldRequestAttributes = RequestContextHolder.getRequestAttributes();
    var req = new MockHttpServletRequest();
    req.setPreferredLocales(List.of(Locale.CHINESE, Locale.FRENCH));
    ServletRequestAttributes servletRequestAttributes = new ServletRequestAttributes(req);
    try {
      RequestContextHolder.setRequestAttributes(servletRequestAttributes);
      Assertions.assertThat(TranslationUtils.getCurrentRequestLocale()).isEqualTo(Locale.CHINESE);
    } finally {
      RequestContextHolder.setRequestAttributes(oldRequestAttributes);
    }
  }
}