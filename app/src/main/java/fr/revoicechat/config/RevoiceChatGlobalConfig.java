package fr.revoicechat.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("revoicechat.global")
public class RevoiceChatGlobalConfig {

  private SeverAppMode severMode;
  private String mediaServerUrl;
  private boolean appOnlyAccessibleByInvitation;

  public SeverAppMode getSeverMode() {
    return severMode;
  }

  public void setSeverMode(final SeverAppMode severMode) {
    this.severMode = severMode;
  }

  public String getMediaServerUrl() {
    return mediaServerUrl;
  }

  public void setMediaServerUrl(final String mediaServerUrl) {
    this.mediaServerUrl = mediaServerUrl;
  }

  public boolean isAppOnlyAccessibleByInvitation() {
    return appOnlyAccessibleByInvitation;
  }

  public void setAppOnlyAccessibleByInvitation(final boolean appOnlyAccessibleByInvitation) {
    this.appOnlyAccessibleByInvitation = appOnlyAccessibleByInvitation;
  }
}
