package fr.revoicechat.core.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Test;

import fr.revoicechat.core.junit.CleanDatabase;
import fr.revoicechat.core.model.InvitationLink;
import fr.revoicechat.core.model.InvitationLinkStatus;
import fr.revoicechat.core.model.InvitationType;
import fr.revoicechat.core.model.User;
import fr.revoicechat.core.model.UserType;
import fr.revoicechat.core.quarkus.profile.BasicIntegrationTestProfile;
import fr.revoicechat.core.representation.user.SignupRepresentation;
import fr.revoicechat.core.representation.user.UserRepresentation;
import fr.revoicechat.security.utils.PasswordUtils;
import fr.revoicechat.core.service.TestUserServiceNoNeedInvitation.AppOnlyAccessibleByInvitationFalse;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@CleanDatabase
@TestProfile(AppOnlyAccessibleByInvitationFalse.class)
class TestUserServiceNoNeedInvitation {

  @Inject EntityManager entityManager;
  @Inject UserService userService;

  @Test
  void testWithNoLink() {
    userService.create(new SignupRepresentation("master", "psw", "master@revoicechat.fr", null));
    SignupRepresentation signer = new SignupRepresentation("user", "test", "user@revoicechat.fr", null);
    var resultRepresentation = userService.create(signer);
    assertThat(resultRepresentation).isNotNull();
    assertUser(resultRepresentation);
  }

  @Test
  void testWithRandomLink() {
    userService.create(new SignupRepresentation("master", "psw", "master@revoicechat.fr", null));
    SignupRepresentation signer = new SignupRepresentation("user", "test", "user@revoicechat.fr", UUID.randomUUID());
    var resultRepresentation = userService.create(signer);
    assertThat(resultRepresentation).isNotNull();
    assertUser(resultRepresentation);
  }

  @Test
  @Transactional
  void testWithInvitationLink() {
    var adminRep = userService.create(new SignupRepresentation("master", "psw", "master@revoicechat.fr", UUID.randomUUID()));
    var admin = entityManager.find(User.class, adminRep.id());
    var invitation = generateInvitationLink(admin);
    SignupRepresentation signer = new SignupRepresentation("user", "test", "user@revoicechat.fr", invitation.getId());
    var resultRepresentation = userService.create(signer);
    assertThat(resultRepresentation).isNotNull();
    assertUser(resultRepresentation);
    entityManager.refresh(invitation);
    assertThat(invitation.getStatus()).isEqualTo(InvitationLinkStatus.CREATED);
    assertThat(invitation.getApplier()).isNull();
  }

  private void assertUser(final UserRepresentation resultRepresentation) {
    var result = entityManager.find(User.class, resultRepresentation.id());
    assertThat(result).isNotNull();
    assertThat(result.getCreatedDate()).isNotNull();
    assertThat(result.getLogin()).isEqualTo("user");
    assertThat(result.getDisplayName()).isEqualTo("user");
    assertThat(result.getPassword()).matches(password -> PasswordUtils.matches("test", password));
    assertThat(result.getEmail()).isEqualTo("user@revoicechat.fr");
    assertThat(result.getType()).isEqualTo(UserType.USER);
  }

  private InvitationLink generateInvitationLink(final User admin) {
    var invitation = new InvitationLink();
    invitation.setId(UUID.randomUUID());
    invitation.setStatus(InvitationLinkStatus.CREATED);
    invitation.setType(InvitationType.APPLICATION_JOIN);
    invitation.setSender(admin);
    invitation.setTargetedServer(null);
    entityManager.persist(invitation);
    return invitation;
  }

  public static class AppOnlyAccessibleByInvitationFalse extends BasicIntegrationTestProfile {
    @Override
    public Map<String, String> getConfigOverrides() {
      var map = new HashMap<>(super.getConfigOverrides());
      map.put("revoicechat.global.app-only-accessible-by-invitation", "false");
      return map;
    }
  }
}