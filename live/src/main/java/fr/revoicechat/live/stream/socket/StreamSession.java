package fr.revoicechat.live.stream.socket;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

record StreamSession(Streamer streamer, Set<Viewer> viewers) {

  public StreamSession(final Streamer streamer) {
    this(streamer, ConcurrentHashMap.newKeySet());
  }

  public void remove(final Viewer viewer) {
    viewers.remove(viewer);
  }
}