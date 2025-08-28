package fr.revoicechat.core.service.server;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import fr.revoicechat.core.repository.ServerRepository;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestMultiServerProviderService {

  @Mock private ServerRepository serverRepository;
  @InjectMocks private MultiServerProviderService serverProviderService;

  @Test
  void testGetServers() {
    serverProviderService.getServers();
    verify(serverRepository).findAll();
  }

  @Test
  void testCanBeUsed() {
    assertThatCode(serverProviderService::canBeUsed).doesNotThrowAnyException();
  }
}