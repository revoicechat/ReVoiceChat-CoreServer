package fr.revoicechat.dev;

import java.time.LocalDateTime;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import fr.revoicechat.model.User;
import fr.revoicechat.security.utils.PasswordUtils;

@ApplicationScoped
@Transactional
public class UserCreator {

  private final EntityManager entityManager;

  UserCreator(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public void add(final String login, String displayName, final String mail) {
    var user = new User();
    user.setId(UUID.randomUUID());
    user.setLogin(login);
    user.setDisplayName(displayName);
    user.setPassword(PasswordUtils.encodePassword("psw"));
    user.setEmail(mail);
    user.setCreatedDate(LocalDateTime.now());
    entityManager.persist(user);
  }
}