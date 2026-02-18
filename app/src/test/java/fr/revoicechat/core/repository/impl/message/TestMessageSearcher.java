package fr.revoicechat.core.repository.impl.message;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import fr.revoicechat.core.junit.CleanDatabase;
import fr.revoicechat.core.model.Message;
import fr.revoicechat.core.model.Room;
import fr.revoicechat.core.model.RoomType;
import fr.revoicechat.core.model.Server;
import fr.revoicechat.core.model.ServerType;
import fr.revoicechat.core.model.User;
import fr.revoicechat.core.quarkus.profile.BasicIntegrationTestProfile;
import fr.revoicechat.core.representation.message.MessageFilterParams;
import fr.revoicechat.notification.model.ActiveStatus;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.junit.TestProfile;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;

@QuarkusTest
@CleanDatabase
@TestProfile(BasicIntegrationTestProfile.class)
@Transactional
class TestMessageSearcher {

  AtomicInteger id = new AtomicInteger(0);
  AtomicInteger minutes = new AtomicInteger(0);

  @Inject EntityManager entityManager;
  @Inject MessageSearcher messageSearcher;

  private User user1;
  private User user2;
  private Room room1;
  private Room room2;

  Map<String, Message> messages;

  @BeforeEach
  void setUp() {
    messages = new HashMap<>();
    user1 = createUser();
    user2 = createUser();
    var server = createServer(user1);
    room1 = createRoom(server);
    messages.put("message 1", createMessage(user1, room1, "message 1"));
    messages.put("message 2", createMessage(user1, room1, "message 2"));
    messages.put("message 3", createMessage(user1, room1, "message 3"));
    messages.put("message 4", createMessage(user1, room1, "message 4"));
    messages.put("message 5", createMessage(user1, room1, "message 5 with keyword"));
    messages.put("message 6", createMessage(user2, room1, "message 6"));
    messages.put("message 7", createMessage(user2, room1, "message 7"));
    messages.put("message 8", createMessage(user2, room1, "message 8"));
    messages.put("message 9", createMessage(user2, room1, "message 9"));
    messages.put("message 10", createMessage(user2, room1, "message 10 with keyword"));
    room2 = createRoom(server);
    messages.put("message 11", createMessage(user1, room2, "message 11"));
    messages.put("message 12", createMessage(user1, room2, "message 12"));
    messages.put("message 13", createMessage(user1, room2, "message 13"));
    messages.put("message 14", createMessage(user1, room2, "message 14"));
    messages.put("message 15", createMessage(user1, room2, "message 15 with keyword"));
    messages.put("message 16", createMessage(user2, room2, "message 16"));
    messages.put("message 17", createMessage(user2, room2, "message 17"));
    messages.put("message 18", createMessage(user2, room2, "message 18"));
    messages.put("message 19", createMessage(user2, room2, "message 19"));
    messages.put("message 20", createMessage(user2, room2, "message 20 with keyword"));
  }

  @Test
  void testAll() {
    // Given
    var param = paramsBuilder().setPage(0).setSize(messages.size()).build();
    // When
    var result = messageSearcher.search(param);
    // Then
    assertThat(result.pageNumber()).isZero();
    assertThat(result.pageSize()).isEqualTo(messages.size());
    assertThat(result.totalElements()).isEqualTo(messages.size());
    var content = result.content();
    assertThat(content).containsExactlyInAnyOrderElementsOf(messages.values());
  }

  @Test
  void testOnRoom1() {
    // Given
    var param = paramsBuilder().setPage(0).setSize(2)
                               .setRoomId(room1.getId())
                               .build();
    // When
    var result = messageSearcher.search(param);
    // Then
    assertThat(result.pageNumber()).isZero();
    assertThat(result.pageSize()).isEqualTo(2);
    assertThat(result.totalElements()).isEqualTo(10);
    var content = result.content();
    assertThat(content).hasSize(2).containsExactlyInAnyOrder(
        messages.get("message 10"),
        messages.get("message 9")
    );
  }

  @Test
  void testOnRoom1ForLastMessage() {
    // Given
    var param = paramsBuilder().setPage(0).setSize(2)
                               .setRoomId(room1.getId())
                               .setLastMessage(messages.get("message 9").getId())
                               .build();
    // When
    var result = messageSearcher.search(param);
    // Then
    assertThat(result.pageNumber()).isZero();
    assertThat(result.pageSize()).isEqualTo(2);
    assertThat(result.totalElements()).isEqualTo(8);
    var content = result.content();
    assertThat(content).hasSize(2).containsExactlyInAnyOrder(
        messages.get("message 8"),
        messages.get("message 7")
    );
  }

  @Test
  void testOnRoom2() {
    // Given
    var param = paramsBuilder().setPage(0).setSize(2)
                               .setRoomId(room2.getId())
                               .build();
    // When
    var result = messageSearcher.search(param);
    // Then
    assertThat(result.pageNumber()).isZero();
    assertThat(result.pageSize()).isEqualTo(2);
    assertThat(result.totalElements()).isEqualTo(10);
    var content = result.content();
    assertThat(content).hasSize(2).containsExactlyInAnyOrder(
        messages.get("message 20"),
        messages.get("message 19")
    );
  }

  @ParameterizedTest
  @ValueSource(strings = { "keyword", "KEYWORD", "KeYwOrD" })
  void testOnRoom1ForKeyword(String keyword) {
    // Given
    var param = paramsBuilder().setPage(0).setSize(2)
                               .setRoomId(room1.getId())
                               .setKeyword(keyword)
                               .build();
    // When
    var result = messageSearcher.search(param);
    // Then
    assertThat(result.pageNumber()).isZero();
    assertThat(result.pageSize()).isEqualTo(2);
    assertThat(result.totalElements()).isEqualTo(2);
    var content = result.content();
    assertThat(content).hasSize(2).containsExactlyInAnyOrder(
        messages.get("message 10"),
        messages.get("message 5")
    );
  }

  @Test
  void testOnRoom1ForEmptyKeyword() {
    // Given
    var param = paramsBuilder().setPage(0).setSize(2)
                               .setRoomId(room1.getId())
                               .setKeyword("")
                               .build();
    // When
    var result = messageSearcher.search(param);
    // Then
    assertThat(result.pageNumber()).isZero();
    assertThat(result.pageSize()).isEqualTo(2);
    assertThat(result.totalElements()).isEqualTo(10);
    var content = result.content();
    assertThat(content).hasSize(2).containsExactlyInAnyOrder(
        messages.get("message 10"),
        messages.get("message 9")
    );
  }

  @Test
  void testMessageOfUser1() {
    // Given
    var param = paramsBuilder().setPage(0).setSize(8)
                               .setUserId(user1.getId())
                               .build();
    // When
    var result = messageSearcher.search(param);
    // Then
    assertThat(result.pageNumber()).isZero();
    assertThat(result.totalElements()).isEqualTo(10);
    var content = result.content();
    assertThat(content).hasSize(8)
        .allMatch(message -> Objects.equals(message.getUser(), user1))
        .allMatch(message -> !Objects.equals(message.getUser(), user2));
  }

  private User createUser() {
    User user = new User();
    user.setId(UUID.randomUUID());
    user.setCreatedDate(OffsetDateTime.now());
    user.setStatus(ActiveStatus.ONLINE);
    user.setLogin("user_" + id.getAndIncrement());
    user.setDisplayName(user.getLogin());
    user.setPassword("123");
    entityManager.persist(user);
    return user;
  }

  private Server createServer(final User user) {
    Server server = new Server();
    server.setId(UUID.randomUUID());
    server.setName("server_" + id.getAndIncrement());
    server.setType(ServerType.PUBLIC);
    server.setOwner(user);
    entityManager.persist(server);
    return server;
  }

  private Room createRoom(final Server server) {
    var room = new Room();
    room.setId(UUID.randomUUID());
    room.setName("room_" + id.getAndIncrement());
    room.setType(RoomType.TEXT);
    room.setServer(server);
    entityManager.persist(room);
    return room;
  }

  private Message createMessage(User user, Room room, String content) {
    var message = new Message();
    message.setId(UUID.randomUUID());
    message.setRoom(room);
    message.setCreatedDate(OffsetDateTime.now().plusMinutes(minutes.getAndIncrement()));
    message.setUser(user);
    message.setText(content);
    entityManager.persist(message);
    return message;
  }

  public static MessageFilterParamsBuilder paramsBuilder() {
    return new MessageFilterParamsBuilder();
  }

  public static class MessageFilterParamsBuilder {
    MessageFilterParams params = new MessageFilterParams();


    public MessageFilterParamsBuilder setPage(final int page) {
      params.setPage(page);
      return this;
    }

    public MessageFilterParamsBuilder setSize(final int size) {
      params.setSize(size);
      return this;
    }

    public MessageFilterParamsBuilder setKeyword(final String keyword) {
      params.setKeyword(keyword);
      return this;
    }

    public MessageFilterParamsBuilder setLastMessage(final UUID lastMessage) {
      params.setLastMessage(lastMessage);
      return this;
    }

    public MessageFilterParamsBuilder setRoomId(final UUID roomId) {
      params.setRoomId(roomId);
      return this;
    }

    public MessageFilterParamsBuilder setUserId(final UUID userId) {
      params.setUserId(userId);
      return this;
    }

    public MessageFilterParams build() {
      return params;
    }
  }
}