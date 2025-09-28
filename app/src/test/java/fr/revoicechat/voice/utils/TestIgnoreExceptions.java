package fr.revoicechat.voice.utils;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;

import org.junit.jupiter.api.Test;

import fr.revoicechat.voice.utils.IgnoreExceptions.ExceptionRunner;
import io.quarkus.test.junit.QuarkusTest;

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