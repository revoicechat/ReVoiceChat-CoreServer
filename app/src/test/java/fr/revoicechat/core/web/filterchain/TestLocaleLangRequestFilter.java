package fr.revoicechat.core.web.filterchain;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URISyntaxException;
import java.util.Locale;

import org.jboss.resteasy.core.interception.jaxrs.ResponseContainerRequestContext;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.junit.jupiter.api.Test;

import fr.revoicechat.core.nls.http.CurrentRequestHolder;

class TestLocaleLangRequestFilter {

  @Test
  void test() throws URISyntaxException {
    var request = MockHttpRequest.get("/test");
    request.header("Accept-Language", "fr");
    var context = new ResponseContainerRequestContext(request);
    assertThat(CurrentRequestHolder.getLocale()).hasSize(1).containsExactly(Locale.ENGLISH);
    new LocaleLangRequestFilter().filter(context);
    assertThat(CurrentRequestHolder.getLocale()).hasSize(2).containsExactly(Locale.FRENCH, Locale.ENGLISH);
    new LocaleLangRequestFilter().filter(context, null);
    assertThat(CurrentRequestHolder.getLocale()).hasSize(1).containsExactly(Locale.ENGLISH);
  }
}