package fr.revoicechat.live.stream.socket;

import java.util.UUID;

import fr.revoicechat.live.risk.LiveDiscussionRisks;
import fr.revoicechat.live.common.socket.SessionHolder;

public interface StreamSession extends SessionHolder {
  UUID user();
  LiveDiscussionRisks risks();
}
