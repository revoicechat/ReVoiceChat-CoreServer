package fr.revoicechat.core.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.Test;

class TestRoom {

  @Test
  @SuppressWarnings({"java:S5838", "java:S5863"})
  void test() {
    var id1 = UUID.randomUUID();
    var room1 = new Room();
    room1.setId(id1);
    var room2 = new Room();
    room2.setId(id1);
    var room3 = new Room();
    room3.setId(UUID.randomUUID());

    assertThat(room1).isEqualTo(room1)
                     .isEqualTo(room2)
                     .hasSameHashCodeAs(room2)
                     .isNotEqualTo(room3)
                     .isNotEqualTo(null)
                     .isNotEqualTo(new Object());
  }
}