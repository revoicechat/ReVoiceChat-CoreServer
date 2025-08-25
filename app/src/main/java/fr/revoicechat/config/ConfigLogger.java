package fr.revoicechat.config;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Startup
public class ConfigLogger {
  private static final Logger LOG = LoggerFactory.getLogger(ConfigLogger.class);

  @PostConstruct
  void logAllConfig() {
    if (LOG.isDebugEnabled()) {
      Config config = ConfigProvider.getConfig();
      config.getPropertyNames()
            .forEach(name -> {
              String value = config.getOptionalValue(name, String.class).orElse("undefined");
              LOG.debug("{} = {}", name, value);
            });
    }
  }
}
