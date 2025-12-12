package fr.revoicechat.core.config;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class SeverAppMode {

  public static final String MONO_SERVER = "MONO_SERVER";
  public static final String MULTI_SERVER = "MULTI_SERVER";

  private final String severMode;

  public SeverAppMode(@ConfigProperty(name = "revoicechat.global.sever-mode") String severMode) {
    this.severMode = severMode;
  }

  public boolean isMonoMode() {
    return MONO_SERVER.equals(severMode);
  }
}