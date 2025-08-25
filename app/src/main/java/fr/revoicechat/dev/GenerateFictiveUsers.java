package fr.revoicechat.dev;

import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.revoicechat.repository.UserRepository;
import io.quarkus.arc.profile.IfBuildProfile;
import io.quarkus.runtime.Startup;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
@Startup
@IfBuildProfile(anyOf = { "create-false-user", "dev" })
public class GenerateFictiveUsers {
  private static final Logger LOG = LoggerFactory.getLogger(GenerateFictiveUsers.class);

  private final UserRepository userRepository;
  private final UserCreator userCreator;

  @ConfigProperty(name = "revoicechat.test.generate-fictive-users")
  boolean canBeCalled;

  GenerateFictiveUsers(UserRepository userRepository, final UserCreator userCreator) {
    LOG.info("GenerateFictiveUsers built");
    this.userRepository = userRepository;
    this.userCreator = userCreator;
  }

  @PostConstruct
  public void init() {
    if (canBeCalled) {
      if (userRepository.count() == 0) {
        LOG.info("default admin user generated");
        userCreator.add("user", "The user", "-no-email-");
        userCreator.add("admin", "The admin", "--no-email--");
        userCreator.add("rex_woof", "Rex_Woof", "---no-email---");
        userCreator.add("nyphew", "Nyphew", "no-email");
      } else {
        LOG.info("db has already user in it");
      }
    }
  }
}
