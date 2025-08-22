package fr.revoicechat.web;

import static org.assertj.core.api.Assertions.*;

import java.util.UUID;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import fr.revoicechat.error.BadRequestException;
import fr.revoicechat.error.ResourceNotFoundException;
import fr.revoicechat.junit.ClearDbAndConnectUser;
import fr.revoicechat.model.RoomType;
import fr.revoicechat.model.Server;
import fr.revoicechat.nls.ServerErrorCode;
import fr.revoicechat.representation.room.RoomRepresentation;
import fr.revoicechat.representation.server.ServerCreationRepresentation;
import fr.revoicechat.representation.user.SignupRepresentation;
import fr.revoicechat.security.UserHolder;
import fr.revoicechat.stub.IntegrationTestConfiguration;
import fr.revoicechat.web.api.ServerController;

/** @see ServerControllerImpl */
class TestServerController {

  @Nested
  @ActiveProfiles({ "test", "test-h2" })
  @DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
  @SpringBootTest(classes = IntegrationTestConfiguration.class, properties = "revoicechat.global.sever-mode=MONO_SERVER")
  @ClearDbAndConnectUser
  class MonoServerController {

    @Inject ServerController controller;
    @Inject AuthController authController;

    @Test
    void creationIsImpossible() {
      var representation = new ServerCreationRepresentation("test");
      assertThatThrownBy(() -> controller.createServer(representation)).isInstanceOf(BadRequestException.class)
                                                                       .hasMessageContaining(ServerErrorCode.APPLICATION_DOES_NOT_ALLOW_SERVER_CREATION.translate());
    }

    @Test
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
    void fetchUser() {
      authController.signup(new SignupRepresentation("Nyphew", "a", "nyphew@mail.com"));
      var servers = controller.getServers();
      assertThat(servers).hasSize(1);
      var server = servers.getFirst();
      var users = controller.fetchUsers(server.getId());
      assertThat(users).hasSize(2);
    }
  }

  @Nested
  @ActiveProfiles({ "test", "test-h2" })
  @DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
  @SpringBootTest(classes = IntegrationTestConfiguration.class, properties = "revoicechat.global.sever-mode=MULTI_SERVER")
  @ClearDbAndConnectUser
  class MultiServerController {

    @Inject UserHolder holder;
    @Inject ServerController controller;
    @Inject AuthController authController;

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
    void fetchUser() {
      authController.signup(new SignupRepresentation("Nyphew", "a", "nyphew@mail.com"));
      var server = controller.createServer(new ServerCreationRepresentation("test"));
      var users = controller.fetchUsers(server.getId());
      assertThat(users).hasSize(1);
    }
  }
}