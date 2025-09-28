package fr.revoicechat.voice.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class IgnoreExceptions {
  private static final Logger LOG = LoggerFactory.getLogger(IgnoreExceptions.class);

  private IgnoreExceptions() {/*not instantiable*/}

  public static void run(ExceptionRunner runner) {
    try {
      runner.run();
    } catch (Exception e) {
      LOG.error("Error ", e);
    }
  }

  @FunctionalInterface
  public interface ExceptionRunner {
    @SuppressWarnings("java:S112") // the goal here is to ignore any exception
    void run() throws Exception;
  }
}
