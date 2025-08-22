package fr.revoicechat.web;

import java.io.IOError;
import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class VoiceSignalingHandler extends TextWebSocketHandler {
  private static final Logger LOG = LoggerFactory.getLogger(VoiceSignalingHandler.class);
  private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

  @Override
  public void afterConnectionEstablished(@NonNull WebSocketSession session) {
    LOG.info("ConnectionEstablished");
    sessions.add(session);
  }

  @Override
  protected void handleTextMessage(@NonNull WebSocketSession session, @NonNull TextMessage message) {
    LOG.debug("handleTextMessage: {}", message);
    sessions.stream()
            .filter(WebSocketSession::isOpen)
            .filter(Predicate.not(session::equals))
            .forEach(s -> {
              try {
                s.sendMessage(message);
              } catch (IOException e) {
                throw new IOError(e);
              }
            });
  }

  @Override
  public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
    LOG.info("ConnectionClosed");
    sessions.remove(session);
  }
}
