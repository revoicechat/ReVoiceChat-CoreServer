package fr.revoicechat.service.sse;

import static fr.revoicechat.representation.sse.SseData.SseTypeData.*;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.ws.rs.sse.SseEventSink;

import org.jboss.resteasy.plugins.providers.sse.SseImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.revoicechat.model.User;
import fr.revoicechat.representation.message.MessageRepresentation;
import fr.revoicechat.representation.sse.SseData;
import fr.revoicechat.service.user.RoomUserFinder;

/**
 * Service that manages textual chat messages and real-time updates via Server-Sent Events (SSE).
 * <p>
 * This service allows clients to:
 * <ul>
 *     <li>Retrieve all messages for a given chat room</li>
 *     <li>Register for real-time message updates using SSE</li>
 *     <li>Send messages to a room and broadcast them to connected clients</li>
 * </ul>
 * <p>
 * SSE emitters are stored in-memory per room and removed automatically when a connection
 * completes, times out, or encounters an error.
 */
@ApplicationScoped
public class TextualChatService {
  private static final Logger LOG = LoggerFactory.getLogger(TextualChatService.class);

  private final Map<UUID, Collection<SseHolder>> processors = new ConcurrentHashMap<>();
  private final RoomUserFinder roomUserFinder;

  public TextualChatService(RoomUserFinder roomUserFinder) {
    this.roomUserFinder = roomUserFinder;
  }

  /**
   * Returns a hot Multi for a user’s SSE stream.
   */
  public void register(UUID userId, SseEventSink sink) {
    getProcessor(userId).add(new SseHolder(sink));
  }

  /**
   * Broadcast a message to all users in a room.
   */
  public void send(UUID roomId, MessageRepresentation message) {
    roomUserFinder.find(roomId)
                  .map(User::getId)
                  .forEach(userId -> sendAndCloseIfNecessary(message, userId));
  }

  private void sendAndCloseIfNecessary(final MessageRepresentation message, final UUID userId) {
    var holders = getProcessor(userId);
    var emitters = new HashSet<>(holders);
    for (SseHolder holder : emitters) {
      LOG.debug("send message to user {}", userId);
      var sent = holder.send(new SseData(ROOM_MESSAGE, message));
      if (!sent) {
        LOG.debug("sse closed for user {}", userId);
        holders.remove(holder);
      }
    }
  }

  public boolean isRegister(User user) {
    return ping(user);
  }

  private boolean ping(User user) {
    LOG.debug("ping user {}", user.getId());
    return getProcessor(user.getId()).stream().anyMatch(holder -> holder.send(new SseData(PING)));
  }

  private Collection<SseHolder> getProcessor(UUID userId) {
    return processors.computeIfAbsent(userId, id -> Collections.synchronizedSet(new HashSet<>()));
  }

  public void shutdownSseEmitters() {
    LOG.info("Closing all SSE connections..");
    processors.values().stream().flatMap(Collection::stream).forEach(holder -> holder.sink.close());
    processors.clear();
  }

  private record SseHolder(SseEventSink sink) {
    boolean send(SseData data) {
      try {
        sink.send(new SseImpl().newEventBuilder().data(data).build());
        return true;
      } catch (Exception e) {
        sink.close();
        return false;
      }
    }
  }
}
