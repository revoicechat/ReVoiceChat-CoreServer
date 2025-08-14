package fr.revoicechat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("revoicechat.global")
public class RevoiceChatGlobalConfig {

  private SeverAppMode severMode;

  public SeverAppMode getSeverMode() {
    return severMode;
  }

  public void setSeverMode(final SeverAppMode severMode) {
    this.severMode = severMode;
  }
}
