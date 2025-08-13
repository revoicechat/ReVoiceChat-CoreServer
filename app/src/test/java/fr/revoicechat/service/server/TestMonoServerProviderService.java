package fr.revoicechat.service.server;

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

import fr.revoicechat.model.Server;
import fr.revoicechat.repository.ServerRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestMonoServerProviderService {

  @Mock private ServerRepository serverRepository;
  @Mock private NewServerCreator newServerCreator;
  @InjectMocks private MonoServerProviderService serverProviderService;

  @BeforeEach
  void setUp() {
    doAnswer(invocationOnMock -> invocationOnMock.getArgument(0))
        .when(serverRepository).save(any(Server.class));
  }

  @Test
  void testGetServerWithOneResult() {
    Server server = new Server();
    doReturn(List.of(server)).when(serverRepository).findAll();
    var result = serverProviderService.getServers();
    assertThat(result).hasSize(1);
    assertThat(result.getFirst()).isSameAs(server);
  }

  @Test
  void testGetServerWithNoResult() {
    doReturn(new Server()).when(newServerCreator).create(any(Server.class));
    doReturn(List.of()).when(serverRepository).findAll();
    var result = serverProviderService.getServers();
    assertThat(result).hasSize(1);
    verify(newServerCreator).create(any());
  }

  @Test
  void testGetServerWithTwoResults() {
    doReturn(List.of(new Server(), new Server())).when(serverRepository).findAll();
    assertThatThrownBy(serverProviderService::getServers)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Yous current application cant be run in mono server because you have more thant one existing server");
  }

  @ParameterizedTest
  @ValueSource(longs = {0, 1})
  void testCanBeUsed(long numberOfServer) {
    doReturn(numberOfServer).when(serverRepository).count();
    assertThatCode(serverProviderService::canBeUsed).doesNotThrowAnyException();
  }

  @ParameterizedTest
  @ValueSource(longs = {2, 3, 4, 5, 6, 7, 8, 9})
  void testCanBeUsedError(long numberOfServer) {
    doReturn(numberOfServer).when(serverRepository).count();
    assertThatThrownBy(serverProviderService::canBeUsed)
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Yous current application cant be run in mono server because you have more thant one existing server");
  }
}