package fr.revoicechat.notification.web;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import fr.revoicechat.core.junit.DBCleaner;
import fr.revoicechat.core.junit.UserCreator;
import fr.revoicechat.core.quarkus.profile.H2Profile;
import fr.revoicechat.notification.NotificationRegistrableHolder;
import fr.revoicechat.notification.service.NotificationService;
import fr.revoicechat.notification.stub.SseEventSinkMock;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import jakarta.ws.rs.sse.SseEventSink;

@QuarkusTest
@TestProfile(H2Profile.class)
class TestNotificationController {

  @Inject DBCleaner cleaner;
  @Inject UserCreator creator;

  @Inject NotificationService service;
  @Inject NotificationRegistrableHolder holder;
  @Inject NotificationController controller;

  @BeforeEach
  void setUp() {
    cleaner.clean();
    creator.create();
  }

  @Test
  @TestSecurity(user = "test-user", roles = {"USER"})
  void test() {
    SseEventSink sink = new SseEventSinkMock();
    controller.generateSseEmitter(sink);
    Assertions.assertThat(service.getProcessor(holder.get().getId())).hasSize(1);
  }
}