package fr.revoicechat.core.service.server;

import static org.assertj.core.api.Assertions.assertThatCode;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.repository.ServerRepository;

class TestMultiServerProviderService {

  @Test
  void testGetServers() {
    var repository = new ServerRepositoryMock(2, List.of(new Server(), new Server()));
    var result = new MultiServerProviderService(repository, null, null, null).getServers();
    Assertions.assertThat(result).hasSize(2);
  }

  @Test
  void testCanBeUsed() {
    var service = new MultiServerProviderService(null, null, null, null);
    assertThatCode(service::canBeUsed).doesNotThrowAnyException();
  }

  private record ServerRepositoryMock(long count, List<Server> findAll) implements ServerRepository {}
}