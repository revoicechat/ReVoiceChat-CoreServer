package fr.revoicechat.dev;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import fr.revoicechat.model.User;
import fr.revoicechat.repository.UserRepository;
import fr.revoicechat.security.PasswordUtil;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;

@Service
@Profile({"create-false-user", "dev"})
class GenerateFictiveUsers {
  private static final Logger LOG = LoggerFactory.getLogger(GenerateFictiveUsers.class);

  private final UserRepository userRepository;

  public GenerateFictiveUsers(final UserRepository userRepository) {this.userRepository = userRepository;}

  @PostConstruct
  @Transactional
  public void init() {
    if (userRepository.count() == 0) {
      LOG.info("default admin user generated");
      addUser("user", "The user", "-no-email-");
      addUser("admin", "The admin", "--no-email--");
      addUser("rex_woof", "Rex_Woof", "---no-email---");
      addUser("nyphew", "Nyphew", "no-email");
    }
  }

  private void addUser(final String login, String displayName, final String mail) {
    var user = new User();
    user.setId(UUID.randomUUID());
    user.setLogin(login);
    user.setDisplayName(displayName);
    user.setPassword(PasswordUtil.encodePassword("psw"));
    user.setEmail(mail);
    user.setCreatedDate(LocalDateTime.now());
    userRepository.save(user);
  }
}
