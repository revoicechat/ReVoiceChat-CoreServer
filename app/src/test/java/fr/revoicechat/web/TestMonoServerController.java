package fr.revoicechat.web;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.revoicechat.error.BadRequestException;
import fr.revoicechat.error.ResourceNotFoundException;
import fr.revoicechat.junit.DBCleaner;
import fr.revoicechat.junit.UserCreator;
import fr.revoicechat.model.RoomType;
import fr.revoicechat.nls.ServerErrorCode;
import fr.revoicechat.quarkus.profile.H2Profile;
import fr.revoicechat.representation.room.RoomRepresentation;
import fr.revoicechat.representation.server.ServerCreationRepresentation;
import fr.revoicechat.representation.user.SignupRepresentation;
import fr.revoicechat.service.RoomService;
import fr.revoicechat.service.ServerService;
import fr.revoicechat.service.UserService;
import fr.revoicechat.web.TestMonoServerController.MonoServerProfile;
import fr.revoicechat.web.api.ServerController;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(MonoServerProfile.class)
class TestMonoServerController {

  @Inject DBCleaner cleaner;
  @Inject UserCreator creator;
  @Inject AuthController authController;
  ServerController controller;
  @Inject ServerService serverService;
  @Inject RoomService roomService;
  @Inject UserService userService;

  @BeforeEach
  void setUp() {
    cleaner.clean();
    creator.create();
    controller = new ServerControllerImpl(serverService, roomService, userService);
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