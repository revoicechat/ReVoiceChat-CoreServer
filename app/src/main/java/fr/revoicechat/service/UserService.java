package fr.revoicechat.service;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import fr.revoicechat.model.ActiveStatus;
import fr.revoicechat.model.User;
import fr.revoicechat.repository.UserRepository;
import fr.revoicechat.representation.user.SignupRepresentation;
import fr.revoicechat.representation.user.UserRepresentation;
import fr.revoicechat.security.PasswordUtil;
import fr.revoicechat.security.UserHolder;
import fr.revoicechat.service.server.ServerProviderService;
import fr.revoicechat.service.sse.TextualChatService;

@Service
public class UserService {

  private final UserRepository userRepository;
  private final UserHolder userHolder;
  private final TextualChatService textualChatService;
  private final ServerProviderService serverProviderService;

  public UserService(final UserRepository userRepository, final UserHolder userHolder, final TextualChatService textualChatService, @Qualifier("serverProviderService") final ServerProviderService serverProviderService) {
    this.userRepository = userRepository;
    this.userHolder = userHolder;
    this.textualChatService = textualChatService;
    this.serverProviderService = serverProviderService;
  }

  public UserRepresentation create(final SignupRepresentation signer) {
    var user = new User();
    user.setId(UUID.randomUUID());
    user.setCreatedDate(LocalDateTime.now());
    user.setDisplayName(signer.username());
    user.setLogin(signer.username());
    user.setEmail(signer.email());
    user.setPassword(PasswordUtil.encodePassword(signer.password()));
    userRepository.save(user);
    return map(user);
  }

  public UserRepresentation findCurrentUser() {
    return map(userHolder.get());
  }

  public UserRepresentation get(final UUID id) {
    return userRepository.findById(id)
                         .map(this::map)
                         .orElseThrow(() -> new UsernameNotFoundException("User not found"));
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
    return textualChatService.isRegister(user) ? ActiveStatus.ONLINE : ActiveStatus.OFFLINE;
  }

  public List<UserRepresentation> fetchUserForServer(final UUID id) {
    return serverProviderService.getUsers(id).map(this::map).toList();
  }
}
