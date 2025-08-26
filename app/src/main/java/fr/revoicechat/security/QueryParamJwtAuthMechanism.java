package fr.revoicechat.security;

import java.util.Optional;
import jakarta.enterprise.context.ApplicationScoped;

import io.quarkus.security.identity.IdentityProviderManager;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.identity.request.TokenAuthenticationRequest;
import io.quarkus.smallrye.jwt.runtime.auth.JsonWebTokenCredential;
import io.quarkus.vertx.http.runtime.security.ChallengeData;
import io.quarkus.vertx.http.runtime.security.HttpAuthenticationMechanism;
import io.smallrye.mutiny.Uni;
import io.vertx.ext.web.RoutingContext;

@ApplicationScoped
public class QueryParamJwtAuthMechanism implements HttpAuthenticationMechanism {

  @Override
  public Uni<SecurityIdentity> authenticate(final RoutingContext context, final IdentityProviderManager identityProviderManager) {
    String jwtToken = context.request().getParam("jwt");
    if (jwtToken == null) {
      return Uni.createFrom().optional(Optional.empty());
    }
    return identityProviderManager.authenticate(new TokenAuthenticationRequest(new JsonWebTokenCredential(jwtToken)));
  }

  @Override
  public Uni<ChallengeData> getChallenge(final RoutingContext context) {
    ChallengeData challenge = new ChallengeData(401, "application/json", "{\"error\": \"Invalid or missing JWT token\"}");
    return Uni.createFrom().item(challenge);
  }
}
