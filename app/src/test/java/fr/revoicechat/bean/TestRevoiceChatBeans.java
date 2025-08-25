package fr.revoicechat.bean;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.ApplicationContext;

import fr.revoicechat.config.RevoiceChatGlobalConfig;
import fr.revoicechat.config.SeverAppMode;
import fr.revoicechat.service.server.MonoServerProviderService;
import fr.revoicechat.service.server.MultiServerProviderService;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TestRevoiceChatBeans {

  @Mock private ApplicationContext applicationContext;
  @InjectMocks private RevoiceChatBeans revoiceChatBeans;

  @BeforeEach
  void setUp() {
    doReturn(mock(MonoServerProviderService.class)).when(applicationContext).getBean(MonoServerProviderService.class);
    doReturn(mock(MultiServerProviderService.class)).when(applicationContext).getBean(MultiServerProviderService.class);
  }

  @Test
  void testMonoServer() {
    RevoiceChatGlobalConfig config = new RevoiceChatGlobalConfig();
    config.setSeverMode(SeverAppMode.MONO_SERVER);
    var result = revoiceChatBeans.serverProviderService(config);
    assertThat(result).isInstanceOf(MonoServerProviderService.class);
    verify(applicationContext).getBean(MonoServerProviderService.class);
  }

  @Test
  void testMultiServer() {
    RevoiceChatGlobalConfig config = new RevoiceChatGlobalConfig();
    config.setSeverMode(SeverAppMode.MULTI_SERVER);
    var result = revoiceChatBeans.serverProviderService(config);
    assertThat(result).isInstanceOf(MultiServerProviderService.class);
    verify(applicationContext).getBean(MultiServerProviderService.class);
  }

  @Test
  void testNullServer() {
    RevoiceChatGlobalConfig config = new RevoiceChatGlobalConfig();
    config.setSeverMode(null);
    assertThatThrownBy(() -> revoiceChatBeans.serverProviderService(config))
        .isInstanceOf(NullPointerException.class);
    verifyNoInteractions(applicationContext);
  }
}