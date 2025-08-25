package fr.revoicechat.nls.http;

import java.util.Locale;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.enterprise.context.RequestScoped;
import jakarta.servlet.http.HttpServletRequest;

@RequestScoped
public class CurrentRequestHolder {

    private static final ThreadLocal<Locale> CURRENT_LOCALE = new ThreadLocal<>();

    private final HttpServletRequest request;

    @SuppressWarnings("unused") // cal by quarkus
    CurrentRequestHolder(final HttpServletRequest request) {
        this.request = request;
    }

    @PostConstruct
    void init() {
        if (request != null) {
            CURRENT_LOCALE.set(request.getLocale());
        } else {
            CURRENT_LOCALE.set(Locale.ENGLISH);
        }
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
