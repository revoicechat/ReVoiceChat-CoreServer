package fr.revoicechat.core.service.server;

import static fr.revoicechat.core.service.server.MonoServerProviderService.ERROR_MESSAGE;
import static org.assertj.core.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.model.User;
import fr.revoicechat.core.repository.ServerRepository;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class TestMonoServerProviderService {

  @Test
  void testGetServerWithOneResult() {
    // Given
    Server server = new Server();
    var newServerCreator = new NewServerCreatorMock();
    var serverRepository = new ServerRepositoryMock(0, List.of(server));
    // When
    var result = new MonoServerProviderService(serverRepository, newServerCreator, null).getServers();
    // Then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst()).isSameAs(server);
    assertThat(newServerCreator.called).isFalse();
  }

  @Test
  void testGetServerWithNoResult() {
    // Given
    var newServerCreator = new NewServerCreatorMock();
    var serverRepository = new ServerRepositoryMock(0, List.of());
    // When
    var result = new MonoServerProviderService(serverRepository, newServerCreator, null).getServers();
    // Then
    assertThat(result).hasSize(1);
    var server = result.getFirst();
    assertThat(server).isNotNull();
    assertThat(server.getName()).isEqualTo("Server");
    assertThat(newServerCreator.called).isTrue();
  }

  @Test
  void testGetServerWithTwoResults() {
    // Given
    var newServerCreator = new NewServerCreatorMock();
    var serverRepository = new ServerRepositoryMock(0, List.of(new Server(), new Server()));
    MonoServerProviderService serverProviderService = new MonoServerProviderService(serverRepository, newServerCreator, null);
    // When - Then
    assertThatThrownBy(serverProviderService::getServers)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(ERROR_MESSAGE);
  }

  @ParameterizedTest
  @ValueSource(longs = { 0, 1 })
  void testCanBeUsed(long numberOfServer) {
    // Given
    var newServerCreator = new NewServerCreatorMock();
    ServerRepository serverRepository = new ServerRepositoryMock(numberOfServer, List.of());
    MonoServerProviderService serverProviderService = new MonoServerProviderService(serverRepository, newServerCreator, null);
    // When - Then
    assertThatCode(serverProviderService::init).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(longs = { 2, 3, 4, 5, 6, 7, 8, 9 })
  void testCanBeUsedError(long numberOfServer) {
    // Given
    var newServerCreator = new NewServerCreatorMock();
    ServerRepository serverRepository = new ServerRepositoryMock(numberOfServer, List.of());
    MonoServerProviderService serverProviderService = new MonoServerProviderService(serverRepository, newServerCreator, null);
    // When - Then
    assertThatThrownBy(serverProviderService::init)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(ERROR_MESSAGE);
  }

  private record ServerRepositoryMock(long count, List<Server> findAll) implements ServerRepository {}

  private static class NewServerCreatorMock extends NewServerCreator {
    private boolean called = false;

    public NewServerCreatorMock() {super(null, new UserHolderMock<>(new User()), null);}

    @Override
    public Server create(final Server server) {
      called = true;
      return server;
    }
  }

}