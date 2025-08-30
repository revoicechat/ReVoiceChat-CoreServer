package fr.revoicechat.core.web;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.revoicechat.core.error.BadRequestException;
import fr.revoicechat.core.error.ResourceNotFoundException;
import fr.revoicechat.core.junit.DBCleaner;
import fr.revoicechat.core.junit.UserCreator;
import fr.revoicechat.core.model.RoomType;
import fr.revoicechat.core.nls.ServerErrorCode;
import fr.revoicechat.core.quarkus.profile.H2Profile;
import fr.revoicechat.core.representation.room.RoomRepresentation;
import fr.revoicechat.core.representation.server.ServerCreationRepresentation;
import fr.revoicechat.core.representation.user.SignupRepresentation;
import fr.revoicechat.core.web.TestMonoServerController.MonoServerProfile;
import fr.revoicechat.core.web.api.ServerController;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

@QuarkusTest
@TestProfile(MonoServerProfile.class)
@TestSecurity(authorizationEnabled = false)
class TestMonoServerController {

  @Inject DBCleaner cleaner;
  @Inject UserCreator creator;
  @Inject AuthController authController;
  @Inject ServerController controller;

  @BeforeEach
  void setUp() {
    cleaner.clean();
    creator.create();
  }

  @Test
  @Transactional
  void creationIsImpossible() {
    var representation = new ServerCreationRepresentation("test");
    assertThatThrownBy(() -> controller.createServer(representation)).isInstanceOf(BadRequestException.class)
                                                                     .hasMessageContaining(ServerErrorCode.APPLICATION_DOES_NOT_ALLOW_SERVER_CREATION.translate());
  }

  @Test
  @Transactional
  void testGetServer() {
    var servers = controller.getServers();
    assertThat(servers).hasSize(1);
    var server = servers.getFirst();
    assertThat(server).isNotNull();
    assertThat(server.getId()).isNotNull();
    assertThat(server.getName()).isEqualTo("Server");
    assertThat(server.getOwner()).isNull();
    assertThat(controller.getServer(server.getId())).isEqualTo(server);
    var randomId = UUID.randomUUID();
    assertThatThrownBy(() -> controller.getServer(randomId)).isInstanceOf(ResourceNotFoundException.class);
    assertThat(controller.getRooms(server.getId())).hasSize(3);
    controller.createRoom(server.getId(), new RoomRepresentation("room", RoomType.TEXT));
    assertThat(controller.getRooms(server.getId())).hasSize(4);
  }

  @Test
  @Transactional
  void fetchUser() {
    authController.signup(new SignupRepresentation("Nyphew", "a", "nyphew@mail.com", UUID.randomUUID()));
    var servers = controller.getServers();
    assertThat(servers).hasSize(1);
    var server = servers.getFirst();
    var users = controller.fetchUsers(server.getId());
    assertThat(users).hasSize(2);
  }

  public static class MonoServerProfile extends H2Profile {
    @Override
    public Map<String, String> getConfigOverrides() {
      var config = new HashMap<>(super.getConfigOverrides());
      config.put("revoicechat.global.app-only-accessible-by-invitation", "false");
      config.put("revoicechat.global.sever-mode", "MONO_SERVER");
      return config;
    }
  }
}