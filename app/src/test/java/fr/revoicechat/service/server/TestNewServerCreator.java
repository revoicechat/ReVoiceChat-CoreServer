package fr.revoicechat.service.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.EntityManager;

import org.assertj.core.api.SoftAssertions;
import org.assertj.core.api.junit.jupiter.SoftAssertionsExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import fr.revoicechat.model.Room;
import fr.revoicechat.model.RoomType;
import fr.revoicechat.model.Server;

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class TestNewServerCreator {

  @Mock private EntityManager entityManager;
  @InjectMocks private NewServerCreator creator;

  @Test
  void test(SoftAssertions softly) {
    // Given
    List<Object> saved = new ArrayList<>();
    doAnswer(invocationOnMock -> {
      Object o = invocationOnMock.getArgument(0);
      saved.add(o);
      return o;
    }).when(entityManager).persist(any());
    Server server = new Server();
    // When
    creator.create(server);
    // Then
    softly.assertThat(server.getId()).isNotNull();
    assertThat(saved).hasSize(4);
    Room room1 = (Room) saved.get(1);
    assertRoom(softly, room1, "General", server, RoomType.TEXT);
    Room room2 = (Room) saved.get(2);
    assertRoom(softly, room2, "Random", server, RoomType.TEXT);
    Room room3 = (Room) saved.get(3);
    assertRoom(softly, room3, "Vocal", server, RoomType.WEBRTC);

    verify(entityManager).persist(server);
    verify(entityManager).persist(room1);
    verify(entityManager).persist(room2);
    verify(entityManager).persist(room3);
    verifyNoMoreInteractions(entityManager);
  }

  private static void assertRoom(final SoftAssertions softly, final Room room1, final String General, final Server server, final RoomType text) {
    softly.assertThat(room1.getId()).isNotNull();
    softly.assertThat(room1.getName()).isEqualTo(General);
    softly.assertThat(room1.getServer()).isEqualTo(server);
    softly.assertThat(room1.getType()).isEqualTo(text);
  }
}