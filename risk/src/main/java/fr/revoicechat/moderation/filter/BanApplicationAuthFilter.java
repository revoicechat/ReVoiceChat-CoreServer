package fr.revoicechat.moderation.filter;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;

import fr.revoicechat.moderation.service.SanctionService;
import io.quarkus.security.identity.SecurityIdentity;

@Provider
@Priority(Priorities.AUTHORIZATION + 1)
public class BanApplicationAuthFilter implements ContainerRequestFilter {

  private final SecurityIdentity identity;
  private final SanctionService sanctionService;

  @Inject
  public BanApplicationAuthFilter(SecurityIdentity identity, SanctionService sanctionService) {
    this.identity = identity;
    this.sanctionService = sanctionService;
  }

  @Override
  public void filter(ContainerRequestContext requestContext) {
    if (identity.isAnonymous()) {
      return;
    }
    if (sanctionService.isAppBanned()) {
      requestContext.abortWith(Response.status(Response.Status.FORBIDDEN)
                                       .entity(new BanResponse("You were banned!"))
                                       .type(MediaType.APPLICATION_JSON)
                                       .build());
    }
  }

  record BanResponse(String message) {}
}