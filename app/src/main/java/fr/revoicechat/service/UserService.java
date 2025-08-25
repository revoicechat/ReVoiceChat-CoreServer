package fr.revoicechat.service;

import static fr.revoicechat.model.InvitationLinkStatus.CREATED;
import static fr.revoicechat.model.InvitationType.APPLICATION_JOIN;
import static fr.revoicechat.nls.UserErrorCode.*;
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

import fr.revoicechat.config.RevoiceChatGlobalConfig;
import fr.revoicechat.error.BadRequestException;
import fr.revoicechat.model.ActiveStatus;
import fr.revoicechat.model.InvitationLink;
import fr.revoicechat.model.InvitationLinkStatus;
import fr.revoicechat.model.User;
import fr.revoicechat.repository.UserRepository;
import fr.revoicechat.representation.user.SignupRepresentation;
import fr.revoicechat.representation.user.UpdatableUserData;
import fr.revoicechat.representation.user.UpdatableUserData.PasswordUpdated;
import fr.revoicechat.representation.user.UserRepresentation;
import fr.revoicechat.security.UserHolder;
import fr.revoicechat.security.utils.PasswordUtils;
import fr.revoicechat.service.server.ServerProviderService;
import fr.revoicechat.service.sse.TextualChatService;

@ApplicationScoped
public class UserService {

  private final EntityManager entityManager;
  private final UserRepository userRepository;
  private final UserHolder userHolder;
  private final TextualChatService textualChatService;
  private final ServerProviderService serverProviderService;
  private final RevoiceChatGlobalConfig globalConfig;

  public UserService(EntityManager entityManager,
                     UserRepository userRepository,
                     UserHolder userHolder,
                     TextualChatService textualChatService,
                     ServerProviderService serverProviderService, final RevoiceChatGlobalConfig globalConfig) {
    this.entityManager = entityManager;
    this.userRepository = userRepository;
    this.userHolder = userHolder;
    this.textualChatService = textualChatService;
    this.serverProviderService = serverProviderService;
    this.globalConfig = globalConfig;
  }

  @Transactional
  public UserRepresentation create(final SignupRepresentation signer) {
    var invitationLink = Optional.ofNullable(signer.invitationLink())
                                 .map(id -> entityManager.find(InvitationLink.class, id))
                                 .orElse(null);
    if (globalConfig.isAppOnlyAccessibleByInvitation() && !isValideInvitation(invitationLink)) {
      throw new BadRequestException(USER_WITH_NO_VALID_INVITATION);
    }
    var user = new User();
    user.setId(UUID.randomUUID());
    user.setCreatedDate(LocalDateTime.now());
    user.setDisplayName(signer.username());
    user.setLogin(signer.username());
    user.setEmail(signer.email());
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
    return Optional.ofNullable(entityManager.find(User.class, id))
                   .map(this::map)
                   .orElseThrow(() -> new NotFoundException("User not found"));
  }

  @Transactional
  public List<UserRepresentation> fetchUserForServer(final UUID id) {
    return serverProviderService.getUsers(id).map(this::map).toList();
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
    return textualChatService.isRegister(user) ? user.getStatus() : ActiveStatus.OFFLINE;
  }
}
