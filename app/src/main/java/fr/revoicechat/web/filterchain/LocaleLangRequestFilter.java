package fr.revoicechat.web.filterchain;

import java.io.IOException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.PreMatching;
import jakarta.ws.rs.ext.Provider;

import fr.revoicechat.nls.http.CurrentRequestHolder;

@Provider
@PreMatching
public class LocaleLangRequestFilter implements ContainerRequestFilter, ContainerResponseFilter {

  @Override
  public void filter(ContainerRequestContext requestContext) {
    CurrentRequestHolder.setLocale(requestContext.getAcceptableLanguages());
  }

  @Override
  public void filter(final ContainerRequestContext requestContext, final ContainerResponseContext responseContext) throws IOException {
    CurrentRequestHolder.removeLocale();
  }
}
