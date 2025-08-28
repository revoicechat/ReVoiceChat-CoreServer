package fr.revoicechat.core.representation.message;

import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class TestCreatedMessageRepresentation {

  @Test
  void testOnEmptyMessage() {
    var creation = new CreatedMessageRepresentation("", List.of());
    Assertions.assertThat(creation.text()).isEmpty();
  }

  @Test
  void testOnMessageNull() {
    var creation = new CreatedMessageRepresentation(null, List.of());
    Assertions.assertThat(creation.text()).isEmpty();
  }

  @Test
  void test() {
    var creation = new CreatedMessageRepresentation("  this is a test  ", List.of());
    Assertions.assertThat(creation.text()).isEqualTo("this is a test");
  }
}