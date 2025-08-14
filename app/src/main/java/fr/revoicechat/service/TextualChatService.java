package fr.revoicechat.service;

import static java.util.Collections.synchronizedSet;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import fr.revoicechat.representation.message.MessageRepresentation;

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
@Service
public class TextualChatService {
  private static final Logger LOG = LoggerFactory.getLogger(TextualChatService.class);

  private final Map<UUID, Collection<SseEmitter>> emitters = new ConcurrentHashMap<>();

  /**
   * Registers a new client connection to receive real-time updates for a given room.
   * <p>
   * The returned {@link SseEmitter} will remain open indefinitely until the client disconnects
   * or an error/timeout occurs. When any of these events happen, the emitter is removed from the registry.
   *
   * @param roomId the unique identifier of the chat room
   * @return the SSE emitter for streaming messages to the client
   */
  public SseEmitter register(final UUID roomId) {
    SseEmitter emitter = new SseEmitter(0L);
    emitter.onCompletion(() -> remove(roomId, emitter, () -> LOG.info("complete")));
    emitter.onError(e -> remove(roomId, emitter, () -> LOG.error("error", e)));
    emitter.onTimeout(() -> remove(roomId, emitter, () -> LOG.error("timeout")));
    getSseEmitters(roomId).add(emitter);
    return emitter;
  }

  /**
   * Sends a new message to all connected clients in a chat room and stores it in the database.
   * <p>
   * If the room does not exist, an exception is thrown.
   *
   * @param roomId  the unique identifier of the chat room
   * @param message the message to send and persist
   * @throws java.util.NoSuchElementException if the room does not exist
   */
  public void send(final UUID roomId, MessageRepresentation message) {
    getSseEmitters(roomId).forEach(sse -> sendSSE(sse, message));
  }

  private Collection<SseEmitter> getSseEmitters(final UUID roomId) {
    return emitters.computeIfAbsent(roomId, key -> synchronizedSet(new HashSet<>()));
  }

  private void sendSSE(final SseEmitter sse, final MessageRepresentation message) {
    try {
      sse.send(message);
    } catch (IOException e) {
      sse.completeWithError(e);
    }
  }

  private void remove(final UUID roomId, SseEmitter emitter, Runnable log) {
    getSseEmitters(roomId).remove(emitter);
    log.run();
  }
}
