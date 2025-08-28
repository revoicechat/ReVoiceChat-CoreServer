package fr.revoicechat.core.web;

import static org.assertj.core.api.Assertions.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.revoicechat.core.error.ResourceNotFoundException;
import fr.revoicechat.core.junit.DBCleaner;
import fr.revoicechat.core.junit.UserCreator;
import fr.revoicechat.core.model.RoomType;
import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.quarkus.profile.H2Profile;
import fr.revoicechat.core.representation.room.RoomRepresentation;
import fr.revoicechat.core.representation.server.ServerCreationRepresentation;
import fr.revoicechat.core.representation.user.SignupRepresentation;
import fr.revoicechat.core.security.UserHolder;
import fr.revoicechat.core.service.RoomService;
import fr.revoicechat.core.service.ServerService;
import fr.revoicechat.core.service.UserService;
import fr.revoicechat.core.web.TestMultiServerController.MultiServerProfile;
import fr.revoicechat.core.web.api.ServerController;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;

@QuarkusTest
@TestProfile(MultiServerProfile.class)
class TestMultiServerController {

  @Inject DBCleaner cleaner;
  @Inject UserCreator creator;
  @Inject UserHolder holder;
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
  void creationPossible() {
    var representation = new ServerCreationRepresentation("test");
    var server = controller.createServer(representation);
    assertThat(server).isNotNull();
    assertThat(server.getId()).isNotNull();
    assertThat(server.getName()).isEqualTo("test");
    var owner = server.getOwner();
    assertThat(owner).isEqualTo(holder.get());
  }

  @Test
  @Transactional
  void testUpdateServer() {
    controller.createServer(new ServerCreationRepresentation("test"));
    var servers = controller.getServers();
    Server server = servers.getFirst();
    assertThat(server.getName()).isEqualTo("test");
    var newName = new ServerCreationRepresentation("new name");
    controller.updateServer(server.getId(), newName);
    server = controller.getServer(server.getId());
    assertThat(server.getName()).isEqualTo("new name");
    var randomId = UUID.randomUUID();
    assertThatThrownBy(() -> controller.updateServer(randomId, newName)).isInstanceOf(ResourceNotFoundException.class);
  }

  @Test
  @Transactional
  void testGetServer() {
    assertThat(controller.getServers()).isEmpty();
    controller.createServer(new ServerCreationRepresentation("test"));
    controller.createServer(new ServerCreationRepresentation("test2"));
    var servers = controller.getServers();
    assertThat(servers).hasSize(2);
    var server = servers.getFirst();
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
    var server = controller.createServer(new ServerCreationRepresentation("test"));
    var users = controller.fetchUsers(server.getId());
    assertThat(users).hasSize(1);
  }

  public static class MultiServerProfile extends H2Profile {
    @Override
    public Map<String, String> getConfigOverrides() {
      var config = new HashMap<>(super.getConfigOverrides());
      config.put("revoicechat.global.app-only-accessible-by-invitation", "false");
      config.put("revoicechat.global.sever-mode", "MULTI_SERVER");
      return config;
    }
  }
}