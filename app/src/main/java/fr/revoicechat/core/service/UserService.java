package fr.revoicechat.core.service;

import static fr.revoicechat.core.model.InvitationLinkStatus.CREATED;
import static fr.revoicechat.core.model.InvitationType.APPLICATION_JOIN;
import static fr.revoicechat.core.nls.UserErrorCode.*;
import static java.util.function.Predicate.not;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.NotFoundException;

import fr.revoicechat.core.config.RevoiceChatGlobalConfig;
import fr.revoicechat.core.error.BadRequestException;
import fr.revoicechat.core.model.ActiveStatus;
import fr.revoicechat.core.model.InvitationLink;
import fr.revoicechat.core.model.InvitationLinkStatus;
import fr.revoicechat.core.model.User;
import fr.revoicechat.core.model.UserType;
import fr.revoicechat.core.repository.UserRepository;
import fr.revoicechat.core.representation.user.AdminUpdatableUserData;
import fr.revoicechat.core.representation.user.SignupRepresentation;
import fr.revoicechat.core.representation.user.UpdatableUserData;
import fr.revoicechat.core.representation.user.UpdatableUserData.PasswordUpdated;
import fr.revoicechat.core.representation.user.UserRepresentation;
import fr.revoicechat.core.security.UserHolder;
import fr.revoicechat.core.security.utils.PasswordUtils;
import fr.revoicechat.core.service.server.ServerProviderService;
import fr.revoicechat.notification.service.NotificationSender;

@ApplicationScoped
public class UserService {

  private final EntityManager entityManager;
  private final UserRepository userRepository;
  private final UserHolder userHolder;
  private final NotificationSender notificationSender;
  private final ServerProviderService serverProviderService;
  private final RevoiceChatGlobalConfig globalConfig;

  public UserService(EntityManager entityManager,
                     UserRepository userRepository,
                     UserHolder userHolder,
                     NotificationSender notificationSender,
                     ServerProviderService serverProviderService,
                     RevoiceChatGlobalConfig globalConfig) {
    this.entityManager = entityManager;
    this.userRepository = userRepository;
    this.userHolder = userHolder;
    this.notificationSender = notificationSender;
    this.serverProviderService = serverProviderService;
    this.globalConfig = globalConfig;
  }

  @Transactional
  public UserRepresentation create(final SignupRepresentation signer) {
    if (userRepository.count() == 0) {
      return generateUser(signer, null, UserType.ADMIN);
    }
    var invitationLink = Optional.ofNullable(signer.invitationLink())
                                 .map(id -> entityManager.find(InvitationLink.class, id))
                                 .orElse(null);
    if (globalConfig.isAppOnlyAccessibleByInvitation() && !isValideInvitation(invitationLink)) {
      throw new BadRequestException(USER_WITH_NO_VALID_INVITATION);
    }
    return generateUser(signer, invitationLink, UserType.USER);
  }

  private UserRepresentation generateUser(SignupRepresentation signer, InvitationLink invitationLink, UserType type) {
    var user = new User();
    user.setId(UUID.randomUUID());
    user.setCreatedDate(LocalDateTime.now());
    user.setDisplayName(signer.username());
    user.setLogin(signer.username());
    user.setEmail(signer.email());
    user.setType(type);
    user.setPassword(PasswordUtils.encodePassword(signer.password()));
    entityManager.persist(user);
    if (invitationLink != null) {
      invitationLink.setStatus(InvitationLinkStatus.USED);
      invitationLink.setApplier(user);
      entityManager.persist(invitationLink);
    }
    return map(user);
  }

  public User findByLogin(final String username) {
    return userRepository.findByLogin(username);
  }

  private static boolean isValideInvitation(final InvitationLink invitationLink) {
    return invitationLink != null
           && APPLICATION_JOIN.equals(invitationLink.getType())
           && CREATED.equals(invitationLink.getStatus());
  }

  public UserRepresentation findCurrentUser() {
    return map(userHolder.get());
  }

  public UserRepresentation get(final UUID id) {
    return map(getUser(id));
  }

  @Transactional
  public List<UserRepresentation> fetchUserForServer(final UUID id) {
    return serverProviderService.getUsers(id).map(this::map).toList();
  }

  @Transactional
  public UserRepresentation updateAsAdmin(final UUID id, final AdminUpdatableUserData userData) {
    var user = getUser(id);
    Optional.ofNullable(userData.displayName()).filter(not(String::isBlank)).ifPresent(user::setDisplayName);
    Optional.ofNullable(userData.type()).ifPresent(user::setType);
    entityManager.persist(user);
    return map(user);
  }

  private User getUser(final UUID id) {
    return Optional.ofNullable(entityManager.find(User.class, id)).orElseThrow(() -> new NotFoundException("User not found"));
  }

  @Transactional
  public UserRepresentation updateConnectedUser(final UpdatableUserData userData) {
    var user = userHolder.get();
    Optional.ofNullable(userData.password()).ifPresent(psw -> setPassword(user, psw));
    Optional.ofNullable(userData.displayName()).filter(not(String::isBlank)).ifPresent(user::setDisplayName);
    Optional.ofNullable(userData.status()).ifPresent(user::setStatus);
    entityManager.persist(user);
    return map(user);
  }

  private void setPassword(final User user, final PasswordUpdated password) {
    if (PasswordUtils.matches(password.password(), user.getPassword())) {
      throw new BadRequestException(USER_PASSWORD_WRONG);
    }
    if (Objects.equals(password.newPassword(), password.confirmPassword())) {
      user.setPassword(PasswordUtils.encodePassword(password.newPassword()));
    } else {
      throw new BadRequestException(USER_PASSWORD_WRONG_CONFIRMATION);
    }
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
    return notificationSender.ping(user) ? user.getStatus() : ActiveStatus.OFFLINE;
  }
}
