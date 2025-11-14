package fr.revoicechat.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

import fr.revoicechat.notification.model.NotificationData;
import fr.revoicechat.notification.model.NotificationRegistrable;
import fr.revoicechat.notification.service.NotificationService.SseHolder;
import fr.revoicechat.notification.stub.NotificationPayloadMock;
import fr.revoicechat.notification.stub.SseEventSinkMock;
import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
class TestNotificationService {

  @Test
  void testRegister() {
    // Given
    var registrable1 = NotificationRegistrable.forId(UUID.randomUUID());
    var registrable2 = NotificationRegistrable.forId(UUID.randomUUID());
    var registrable3 = NotificationRegistrable.forId(UUID.randomUUID());
    var sink1 = new SseEventSinkMock();
    var sink2 = new SseEventSinkMock();
    var sink3 = new SseEventSinkMock();
    // When
    var service = new NotificationService();
    service.register(registrable1, sink1);
    service.register(registrable1, sink2);
    service.register(registrable2, sink3);
    // Then
    assertThat(service.getProcessor(registrable1.getId())).hasSize(2)
                                                          .map(SseHolder::sink).containsExactlyInAnyOrder(sink1, sink2);
    assertThat(service.getProcessor(registrable2.getId())).hasSize(1)
                                                          .map(SseHolder::sink).containsExactlyInAnyOrder(sink3);
    assertThat(service.getProcessor(registrable3.getId())).isEmpty();
  }

  @Test
  void testSend() {
    // Given
    var registrable1 = NotificationRegistrable.forId(UUID.randomUUID());
    var sink1 = new SseEventSinkMock();
    var sink2 = new SseEventSinkMock();
    sink2.close();
    // When
    var service = new NotificationService();
    service.register(registrable1, sink1);
    service.register(registrable1, sink2);
    service.send(Stream.of(registrable1), new NotificationData(new NotificationPayloadMock("test")));
    // Then
    assertThat(service.getProcessor(registrable1.getId())).hasSize(1)
                                                          .map(SseHolder::sink).containsExactlyInAnyOrder(sink1);
    assertThat(sink2.isClosed()).isTrue();
    assertThat(sink1.isClosed()).isFalse();
    assertThat(sink1.getEvents()).hasSize(1);
  }

  @Test
  void testPingWithNoRegistry() {
    var registrable1 = NotificationRegistrable.forId(UUID.randomUUID());
    assertThat(new NotificationService().ping(registrable1)).isFalse();
  }

  @Test
  void testPingWithOneRegistry() {
    var registrable1 = NotificationRegistrable.forId(UUID.randomUUID());
    var sink1 = new SseEventSinkMock();
    var service = new NotificationService();
    service.register(registrable1, sink1);
    assertThat(service.ping(registrable1)).isTrue();
  }

  @Test
  void testPingWithOneRegistryCloseAndOneOpen() {
    var registrable1 = NotificationRegistrable.forId(UUID.randomUUID());
    var sink1 = new SseEventSinkMock();
    var sink2 = new SseEventSinkMock();
    sink2.close();
    var service = new NotificationService();
    service.register(registrable1, sink1);
    service.register(registrable1, sink2);
    assertThat(service.ping(registrable1)).isTrue();
  }

  @Test
  void testPingWithOnlyClosedRegistry() {
    var registrable1 = NotificationRegistrable.forId(UUID.randomUUID());
    var sink1 = new SseEventSinkMock();
    sink1.close();
    var sink2 = new SseEventSinkMock();
    sink2.close();
    var service = new NotificationService();
    service.register(registrable1, sink1);
    service.register(registrable1, sink2);
    assertThat(service.ping(registrable1)).isFalse();
  }

  @Test
  void testShutdownSseEmitters() {
    var registrable1 = NotificationRegistrable.forId(UUID.randomUUID());
    var sink1 = new SseEventSinkMock();
    var sink2 = new SseEventSinkMock();
    var service = new NotificationService();
    service.register(registrable1, sink1);
    service.register(registrable1, sink2);
    service.shutdownSseEmitters();
    assertThat(sink1.isClosed()).isTrue();
    assertThat(sink2.isClosed()).isTrue();
  }
}