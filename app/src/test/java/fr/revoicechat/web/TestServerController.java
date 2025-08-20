package fr.revoicechat.web;

import static org.assertj.core.api.Assertions.*;

import jakarta.inject.Inject;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.context.ActiveProfiles;

import fr.revoicechat.error.BadRequestException;
import fr.revoicechat.junit.ConnectUser;
import fr.revoicechat.nls.ServerErrorCode;
import fr.revoicechat.representation.server.ServerCreationRepresentation;
import fr.revoicechat.security.UserHolder;
import fr.revoicechat.stub.IntegrationTestConfiguration;
import fr.revoicechat.web.api.ServerController;

/**
 * Tests associés à {@link ServerController}.
 */
class TestServerController {

  @Nested
  @ActiveProfiles({ "test", "test-h2" })
  @DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
  @SpringBootTest(classes = IntegrationTestConfiguration.class, properties = "revoicechat.global.sever-mode=MONO_SERVER")
  @ConnectUser
  class MonoServerController {

    @Inject ServerController controller;

    @Test
    void creationIsImpossible() {
      var representation = new ServerCreationRepresentation("test");
      assertThatThrownBy(() -> controller.createServer(representation)).isInstanceOf(BadRequestException.class)
          .hasMessageContaining(ServerErrorCode.APPLICATION_DOES_NOT_ALLOW_SERVER_CREATION.translate());
    }
  }

  @Nested
  @ActiveProfiles({ "test", "test-h2" })
  @DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
  @SpringBootTest(classes = IntegrationTestConfiguration.class, properties = "revoicechat.global.sever-mode=MULTI_SERVER")
  @ConnectUser
  class MultiServerController {

    @Inject UserHolder holder;
    @Inject ServerController controller;

    @Test
    void creationPossible() {
      var representation = new ServerCreationRepresentation("test");
      var server = controller.createServer(representation);
      assertThat(server).isNotNull();
      assertThat(server.getId()).isNotNull();
      assertThat(server.getName()).isEqualTo("test");
      assertThat(server.getOwner()).isEqualTo(holder.get());
    }
  }
}