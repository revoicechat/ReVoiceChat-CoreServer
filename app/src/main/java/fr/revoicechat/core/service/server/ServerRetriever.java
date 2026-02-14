package fr.revoicechat.core.service.server;

import java.util.List;

import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.repository.ServerRepository;
import fr.revoicechat.core.representation.server.ServerRepresentation;
import fr.revoicechat.core.service.ServerService;
import fr.revoicechat.security.UserHolder;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

@ApplicationScoped
public class ServerRetriever {

  private final UserHolder userHolder;
  private final ServerRepository serverRepository;
  private final ServerService serverService;

  public ServerRetriever(final UserHolder userHolder, final ServerRepository serverRepository, final ServerService serverService) {
    this.userHolder = userHolder;
    this.serverRepository = serverRepository;
    this.serverService = serverService;
  }

  /** @return all servers for the connected user. */
  @Transactional
  public List<ServerRepresentation> getAllMyServers() {
    return serverRepository.getByUser(userHolder.get())
                                .map(serverService::map)
                                .toList();
  }

  /** @return all public servers. */
  @Transactional
  public List<ServerRepresentation> getAllPublicServers(final boolean joinedToo) {
    var servers = serverRepository.getPublicServer();
    if (joinedToo) {
      return servers.map(serverService::map).toList();
    } else {
      var serverIds = serverRepository.getByUser(userHolder.get()).map(Server::getId).toList();
      return servers.filter(server -> serverIds.contains(server.getId()))
                    .map(serverService::map)
                    .toList();
    }
  }
}
