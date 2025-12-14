package fr.revoicechat.core.service.server;

import java.util.Optional;
import java.util.UUID;

import fr.revoicechat.core.model.Server;
import fr.revoicechat.risk.service.server.ServerFinder;
import fr.revoicechat.web.error.ResourceNotFoundException;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;

@ApplicationScoped
public class ServerEntityService implements ServerFinder {

  private final EntityManager entityManager;

  public ServerEntityService(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  @Override
  public void existsOrThrow(final UUID id) {
    getEntity(id);
  }

  public Server getEntity(final UUID id) {
    return Optional.ofNullable(entityManager.find(Server.class, id))
                   .orElseThrow(() -> new ResourceNotFoundException(Server.class, id));
  }
}
