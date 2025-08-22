package fr.revoicechat.config;

import jakarta.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class RevoiceChatGlobalConfig {

  @ConfigProperty(name = "revoicechat.global.sever-mode")
  SeverAppMode severMode;
  @ConfigProperty(name = "revoicechat.global.media-server-url")
  String mediaServerUrl;

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
}
