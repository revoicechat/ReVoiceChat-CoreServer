package fr.revoicechat.service;

import static fr.revoicechat.nls.UserErrorCode.*;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import fr.revoicechat.error.BadRequestException;
import fr.revoicechat.model.ActiveStatus;
import fr.revoicechat.model.User;
import fr.revoicechat.representation.user.SignupRepresentation;
import fr.revoicechat.representation.user.UpdatableUserData;
import fr.revoicechat.representation.user.UserRepresentation;
import fr.revoicechat.security.PasswordUtil;
import fr.revoicechat.security.UserHolder;
import fr.revoicechat.service.server.ServerProviderService;
import fr.revoicechat.service.sse.TextualChatService;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@Service
public class UserService {

  private final EntityManager entityManager;
  private final UserHolder userHolder;
  private final TextualChatService textualChatService;
  private final ServerProviderService serverProviderService;

  public UserService(EntityManager entityManager,
                     UserHolder userHolder,
                     TextualChatService textualChatService,
                     ServerProviderService serverProviderService) {
    this.entityManager = entityManager;
    this.userHolder = userHolder;
    this.textualChatService = textualChatService;
    this.serverProviderService = serverProviderService;
  }

  @Transactional
  public UserRepresentation create(final SignupRepresentation signer) {
    var user = new User();
    user.setId(UUID.randomUUID());
    user.setCreatedDate(LocalDateTime.now());
    user.setDisplayName(signer.username());
    user.setLogin(signer.username());
    user.setEmail(signer.email());
    user.setPassword(PasswordUtil.encodePassword(signer.password()));
    entityManager.persist(user);
    return map(user);
  }

  public UserRepresentation findCurrentUser() {
    return map(userHolder.get());
  }

  public UserRepresentation get(final UUID id) {
    return Optional.ofNullable(entityManager.find(User.class, id))
                   .map(this::map)
                   .orElseThrow(() -> new UsernameNotFoundException("User not found"));
  }

  @Transactional
  public List<UserRepresentation> fetchUserForServer(final UUID id) {
    return serverProviderService.getUsers(id).map(this::map).toList();
  }

  @Transactional
  public UserRepresentation updateConnectedUser(final UpdatableUserData userData) {
    var user = userHolder.get();
    if (userData.newPassword() != null) {
      if (PasswordUtil.matches(userData.password(), user.getPassword())) {
        throw new BadRequestException(USER_PASSWORD_WRONG);
      }
      if (Objects.equals(userData.password(), userData.confirmPassword())) {
        user.setPassword(PasswordUtil.encodePassword(userData.password()));
      } else {
        throw new BadRequestException(USER_PASSWORD_WRONG_CONFIRMATION);
      }
    }
    user.setDisplayName(userData.displayName());
    user.setStatus(userData.status());
    entityManager.persist(user);
    return map(user);
  }

  private UserRepresentation map(final User user) {
    return new UserRepresentation(
        user.getId(),
        user.getDisplayName(),
        user.getLogin(),
        user.getCreatedDate().atOffset(ZoneOffset.UTC),
        getActiveStatus(user)
    );
  }

  private ActiveStatus getActiveStatus(final User user) {
    return textualChatService.isRegister(user) ? user.getStatus() : ActiveStatus.OFFLINE;
  }
}
