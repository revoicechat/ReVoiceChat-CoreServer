package fr.revoicechat.core.utils;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import fr.revoicechat.core.utils.IgnoreExceptions.ExceptionRunner;
import io.quarkus.test.junit.QuarkusTest;

/**
 * Tests associés à {@link IgnoreExceptions}.
 */
@QuarkusTest
class TestIgnoreExceptions {

  @Test
  void test() {
    ExceptionRunner runner = () -> {};
    assertThatCode(() -> IgnoreExceptions.run(runner)).doesNotThrowAnyException();
  }

  @Test
  void testWithError() {
    ExceptionRunner runner = () -> {throw new IOException();};
    assertThatCode(() -> IgnoreExceptions.run(runner)).doesNotThrowAnyException();
  }
}