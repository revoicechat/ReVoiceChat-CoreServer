package fr.revoicechat.stub;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import fr.revoicechat.security.UserHolder;

@TestConfiguration
public class IntegrationTestConfiguration {

  @Bean
  @Primary
  public UserHolder userHolder() {
    return new UserHolderMock();
  }
}
