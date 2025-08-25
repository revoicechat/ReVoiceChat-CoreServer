package fr.revoicechat.nls.http;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.RequestScoped;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;

@RequestScoped
public class CurrentRequestHolder {

    private static final ThreadLocal<Locale> CURRENT_LOCALE = new ThreadLocal<>();

    Locale locale;

    public CurrentRequestHolder(@Context HttpHeaders headers) {
        List<Locale> langs = headers.getAcceptableLanguages();
        this.locale = langs.isEmpty() ? Locale.ENGLISH : langs.getFirst();
    }

    @PostConstruct
    void init() {
      CURRENT_LOCALE.set(Objects.requireNonNullElse(locale, Locale.ENGLISH));
    }

    @PreDestroy
    void cleanup() {
        CURRENT_LOCALE.remove();
    }

    public static Locale getLocale() {
        Locale locale = CURRENT_LOCALE.get();
        return locale != null ? locale : Locale.ENGLISH;
    }
}
