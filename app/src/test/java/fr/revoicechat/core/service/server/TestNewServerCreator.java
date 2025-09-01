package fr.revoicechat.core.service.server;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import fr.revoicechat.core.model.Room;
import fr.revoicechat.core.model.RoomType;
import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.stub.EntityManagerMock;

@ExtendWith({ SoftAssertionsExtension.class })
class TestNewServerCreator {

  @Test
  void test(SoftAssertions softly) {
    // Given
    try (var em = new MockEntityManager()) {
      Server server = new Server();
      // When
      new NewServerCreator(em).create(server);
      // Then
      softly.assertThat(server.getId()).isNotNull();
      assertThat(em.saved).hasSize(4);
      Room room1 = (Room) em.saved.get(1);
      assertRoom(softly, room1, "General", server, RoomType.TEXT);
      Room room2 = (Room) em.saved.get(2);
      assertRoom(softly, room2, "Random", server, RoomType.TEXT);
      Room room3 = (Room) em.saved.get(3);
      assertRoom(softly, room3, "Vocal", server, RoomType.WEBRTC);
    }
  }

  private static void assertRoom(final SoftAssertions softly, final Room room1, final String General, final Server server, final RoomType text) {
    softly.assertThat(room1.getId()).isNotNull();
    softly.assertThat(room1.getName()).isEqualTo(General);
    softly.assertThat(room1.getServer()).isEqualTo(server);
    softly.assertThat(room1.getType()).isEqualTo(text);
  }

  private static class MockEntityManager extends EntityManagerMock {
    private final List<Object> saved = new ArrayList<>();

    @Override
    public void persist(final Object o) {
      saved.add(o);
    }
  }
}