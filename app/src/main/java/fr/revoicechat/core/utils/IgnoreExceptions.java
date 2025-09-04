package fr.revoicechat.core.utils;

public final class IgnoreExceptions {

  private IgnoreExceptions() {/*not instantiable*/}

  public static void run(ExceptionRunner runner) {
    try {
      runner.run();
    } catch (Exception ignore) {
      // ignored the following error
    }
  }

  @FunctionalInterface
  public interface ExceptionRunner {
    @SuppressWarnings("java:S112") // the goal here is to ignore any exception
    void run() throws Exception;
  }
}
