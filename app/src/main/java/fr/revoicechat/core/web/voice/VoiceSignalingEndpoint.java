package fr.revoicechat.core.web.voice;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/signal")
public class VoiceSignalingEndpoint {

    private static final Logger LOG = Logger.getLogger(VoiceSignalingEndpoint.class);
    private static final Set<Session> sessions = ConcurrentHashMap.newKeySet();

    @OnOpen
    public void onOpen(Session session) {
        LOG.info("ConnectionEstablished: " + session.getId());
        sessions.add(session);
    }

    @OnMessage
    public void onMessage(String message, Session senderSession) {
        LOG.debugf("handleTextMessage from %s: %s", senderSession.getId(), message);
        sessions.stream()
                .filter(Session::isOpen)
                .filter(s -> !s.equals(senderSession))
                .forEach(session -> {
                    try {
                        session.getBasicRemote().sendText(message);
                    } catch (IOException e) {
                        LOG.error("Error sending message to session " + session.getId(), e);
                    }
                });
    }

    @OnClose
    public void onClose(Session session) {
        LOG.info("ConnectionClosed: " + session.getId());
        sessions.remove(session);
    }
}
