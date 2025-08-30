package fr.revoicechat.core.service.server;

import static fr.revoicechat.core.service.server.MonoServerProviderService.ERROR_MESSAGE;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.repository.ServerRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestMonoServerProviderService {

  @Mock private ServerRepository serverRepository;
  @Mock private NewServerCreator newServerCreator;
  @InjectMocks private MonoServerProviderService serverProviderService;

  @BeforeEach
  void setUp() {
    doAnswer(invocationOnMock -> invocationOnMock.getArgument(0))
        .when(newServerCreator).create(any(Server.class));
  }

  @Test
  void testGetServerWithOneResult() {
    // Given
    Server server = new Server();
    doReturn(List.of(server)).when(serverRepository).findAll();
    // When
    var result = serverProviderService.getServers();
    // Then
    assertThat(result).hasSize(1);
    assertThat(result.getFirst()).isSameAs(server);
  }

  @Test
  void testGetServerWithNoResult() {
    // Given
    doReturn(List.of()).when(serverRepository).findAll();
    // When
    var result = serverProviderService.getServers();
    // Then
    assertThat(result).hasSize(1);
    var server = result.getFirst();
    assertThat(server).isNotNull();
    assertThat(server.getName()).isEqualTo("Server");
    verify(newServerCreator).create(any());
  }

  @Test
  void testGetServerWithTwoResults() {
    // Given
    doReturn(List.of(new Server(), new Server())).when(serverRepository).findAll();
    // When - Then
    assertThatThrownBy(serverProviderService::getServers)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(ERROR_MESSAGE);
  }

  @ParameterizedTest
  @ValueSource(longs = {0, 1})
  void testCanBeUsed(long numberOfServer) {
    // Given
    doReturn(numberOfServer).when(serverRepository).count();
    // When - Then
    assertThatCode(serverProviderService::canBeUsed).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(longs = {2, 3, 4, 5, 6, 7, 8, 9})
  void testCanBeUsedError(long numberOfServer) {
    // Given
    doReturn(numberOfServer).when(serverRepository).count();
    // When - Then
    assertThatThrownBy(serverProviderService::canBeUsed)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage(ERROR_MESSAGE);
  }
}