package fr.revoicechat.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {

  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(@NonNull CorsRegistry registry) {
        registry.addMapping("/**") // apply to all paths
                // TODO - add Origin patterns with config file
                .allowedOriginPatterns("*") // allow all origins
                .allowedMethods("*") // allow all HTTP methods
                .allowedHeaders("*") // allow all headers
                .allowCredentials(true); // must be false when origins = "*"
      }
    };
  }
}
