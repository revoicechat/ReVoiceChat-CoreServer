package fr.revoicechat.service.server;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;

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
import fr.revoicechat.repository.RoomRepository;
import fr.revoicechat.repository.ServerRepository;

@ExtendWith({MockitoExtension.class, SoftAssertionsExtension.class})
class TestNewServerCreator {

  @Mock private ServerRepository serverRepository;
  @Mock private RoomRepository roomRepository;
  @InjectMocks private NewServerCreator creator;

  @Test
  void test(SoftAssertions softly) {
    // Given
    List<Room> rooms = new ArrayList<>();
    doAnswer(invocationOnMock -> {
      Room room = invocationOnMock.getArgument(0);
      rooms.add(room);
      return room;
    }).when(roomRepository).save(any());
    Server server = new Server();
    // When
    creator.create(server);
    // Then
    softly.assertThat(server.getId()).isNotNull();
    assertThat(rooms).hasSize(3);
    var room1 = rooms.get(0);
    assertRoom(softly, room1, "📝 General", server, RoomType.TEXT);
    var room2 = rooms.get(1);
    assertRoom(softly, room2, "📝 Random", server, RoomType.TEXT);
    var room3 = rooms.get(2);
    assertRoom(softly, room3, "🔊 Vocal", server, RoomType.WEBRTC);

    verify(serverRepository).save(server);
    verify(roomRepository).save(room1);
    verify(roomRepository).save(room2);
    verify(roomRepository).save(room3);
    verifyNoMoreInteractions(serverRepository, roomRepository);
  }

  private static void assertRoom(final SoftAssertions softly, final Room room1, final String General, final Server server, final RoomType text) {
    softly.assertThat(room1.getId()).isNotNull();
    softly.assertThat(room1.getName()).isEqualTo(General);
    softly.assertThat(room1.getServer()).isEqualTo(server);
    softly.assertThat(room1.getType()).isEqualTo(text);
  }
}