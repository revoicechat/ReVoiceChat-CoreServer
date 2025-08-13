package fr.revoicechat.service;

import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import fr.revoicechat.model.Server;
import fr.revoicechat.repository.ServerRepository;
import fr.revoicechat.service.server.ServerProviderService;

@Service
public class ServerService {
  private static final Logger LOG = LoggerFactory.getLogger(ServerService.class);

  private final ServerProviderService serverProviderService;
  private final ServerRepository serverRepository;

  public ServerService(ServerProviderService serverProviderService, final ServerRepository serverRepository) {
    this.serverProviderService = serverProviderService;
    this.serverRepository = serverRepository;
  }

  public List<Server> getAll() {
    var servs = serverProviderService.getServers();
    LOG.info("servers LIST / {}", servs);
    return servs;
  }

  public Server get(final UUID id) {
    return serverRepository.findById(id).orElseThrow();
  }

  public Server create(final Server entity) {
    return serverRepository.save(entity);
  }

  public Server update(final UUID id, final Server entity) {
    entity.setId(id);
    return serverRepository.save(entity);
  }
}
