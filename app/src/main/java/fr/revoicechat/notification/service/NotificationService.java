package fr.revoicechat.notification.service;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.jboss.resteasy.plugins.providers.sse.SseImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.revoicechat.notification.model.NotificationData;
import fr.revoicechat.notification.model.NotificationRegistrable;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import jakarta.ws.rs.sse.SseEventSink;

/**
 * Service that manages user notification via Server-Sent Events (SSE).
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
@Singleton
public class NotificationService implements NotificationRegistry, NotificationSender {
  private static final Logger LOG = LoggerFactory.getLogger(NotificationService.class);

  private final Map<UUID, Collection<SseHolder>> processors = new ConcurrentHashMap<>();

  @Override
  public void register(NotificationRegistrable registrable, SseEventSink sink) {
    getProcessor(registrable.getId()).add(new SseHolder(sink));
  }

  @Override
  public void send(Stream<? extends NotificationRegistrable> targetedUsers, NotificationData data) {
    targetedUsers.forEach(user -> sendAndCloseIfNecessary(data, user.getId()));
  }

  /**
   * the SSE holder in wrap in another concurrent hashmap
   */
  @Override
  public boolean ping(NotificationRegistrable registrable) {
    var id = registrable.getId();
    LOG.debug("ping user {}", id);
    var holder = getProcessor(registrable.getId());
    return new HashSet<>(holder).stream().anyMatch(sse -> sse.send(NotificationData.ping()));
  }

  private void sendAndCloseIfNecessary(final NotificationData notificationData, final UUID userId) {
    var holders = getProcessor(userId);
    var emitters = new HashSet<>(holders);
    for (SseHolder holder : emitters) {
      LOG.debug("send message to user {}", userId);
      var sent = holder.send(notificationData);
      if (!sent) {
        LOG.debug("sse closed for user {}", userId);
        holders.remove(holder);
      }
    }
  }

  public Collection<SseHolder> getProcessor(UUID userId) {
    return processors.computeIfAbsent(userId, id -> Collections.synchronizedSet(new HashSet<>()));
  }

  @PreDestroy
  public void shutdownSseEmitters() {
    LOG.info("Closing all SSE connections..");
    processors.values().stream().flatMap(Collection::stream).forEach(SseHolder::close);
    processors.clear();
  }

  public record SseHolder(SseEventSink sink) {
    boolean send(NotificationData data) {
      try {
        sink.send(new SseImpl().newEventBuilder().data(data).build());
        return true;
      } catch (Exception e) {
        sink.close();
        return false;
      }
    }

    void close() {sink.close();}
  }
}
