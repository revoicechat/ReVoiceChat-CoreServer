package fr.revoicechat.risk.stub;

import java.util.UUID;

import fr.revoicechat.risk.service.server.ServerFinder;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ServerFinderMock implements ServerFinder {
  @Override
  public void existsOrThrow(final UUID id) {
    // nothing here
  }
}
