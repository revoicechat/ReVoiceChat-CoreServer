package fr.revoicechat.service.sse;

import static fr.revoicechat.representation.sse.SseData.SseTypeData.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import jakarta.enterprise.context.ApplicationScoped;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.revoicechat.model.User;
import fr.revoicechat.representation.message.MessageRepresentation;
import fr.revoicechat.representation.sse.SseData;
import fr.revoicechat.service.user.RoomUserFinder;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;

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

  private final Map<UUID, BroadcastProcessor<SseData>> processors = new ConcurrentHashMap<>();
  private final RoomUserFinder roomUserFinder;

  public TextualChatService(RoomUserFinder roomUserFinder) {
    this.roomUserFinder = roomUserFinder;
  }

  /**
   * Returns a hot Multi for a user’s SSE stream.
   */
  public Multi<SseData> register(UUID userId) {
    return getProcessor(userId).toHotStream();
  }

  /**
   * Broadcast a message to all users in a room.
   */
  public void send(UUID roomId, MessageRepresentation message) {
    roomUserFinder.find(roomId)
                  .map(User::getId)
                  .map(this::getProcessor)
                  .forEach(proc -> proc.onNext(new SseData(ROOM_MESSAGE, message)));
  }

  public boolean isRegister(User user) {
    return ping(user);
  }

  private boolean ping(User user) {
    BroadcastProcessor<SseData> proc = processors.get(user.getId());
    if (proc != null) {
      proc.onNext(new SseData(PING));
      return true;
    }
    return false;
  }

  private BroadcastProcessor<SseData> getProcessor(UUID userId) {
    return processors.computeIfAbsent(userId, id -> BroadcastProcessor.create());
  }

  public void shutdownSseEmitters() {
    LOG.info("Closing all SSE connections..");
    processors.values().forEach(BroadcastProcessor::onComplete);
    processors.clear();
  }
}
