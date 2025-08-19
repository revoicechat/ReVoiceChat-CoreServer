package fr.revoicechat.dev;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import fr.revoicechat.model.User;
import fr.revoicechat.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

@Service
@Profile("dev")
class GenerateFictiveUsers {
  private static final Logger LOG = LoggerFactory.getLogger(GenerateFictiveUsers.class);

  private final UserRepository userRepository;

  public GenerateFictiveUsers(final UserRepository userRepository) {this.userRepository = userRepository;}

  @PostConstruct
  @Transactional
  public void init() {
    if (userRepository.count() == 0) {
      LOG.info("default admin user generated");
      addUser("user", "-no-email-");
      addUser("admin", "--no-email--");
      addUser("rex_woof", "---no-email---");
      addUser("nyphew", "no-email");
    }
  }

  private void addUser(final String login, final String mail) {
    var user = new User();
    user.setId(UUID.randomUUID());
    user.setLogin(login);
    user.setDisplayName(login);
    user.setPassword("psw");
    user.setEmail(mail);
    user.setCreatedDate(LocalDateTime.now());
    userRepository.save(user);
  }
}
