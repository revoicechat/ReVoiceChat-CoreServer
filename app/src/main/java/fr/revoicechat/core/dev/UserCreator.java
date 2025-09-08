package fr.revoicechat.core.dev;

import java.time.LocalDateTime;
import java.util.UUID;

import fr.revoicechat.core.model.UserType;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import fr.revoicechat.core.model.User;
import fr.revoicechat.security.utils.PasswordUtils;

@ApplicationScoped
@Transactional
public class UserCreator {

  private final EntityManager entityManager;

  UserCreator(EntityManager entityManager) {
    this.entityManager = entityManager;
  }

  public void add(String login, String displayName, String mail, UserType type) {
    var user = new User();
    user.setId(UUID.randomUUID());
    user.setLogin(login);
    user.setDisplayName(displayName);
    user.setPassword(PasswordUtils.encodePassword("psw"));
    user.setEmail(mail);
    user.setCreatedDate(LocalDateTime.now());
    user.setType(type);
    entityManager.persist(user);
  }
}