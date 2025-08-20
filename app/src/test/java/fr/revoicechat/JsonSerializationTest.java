package fr.revoicechat;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
class JsonSerializationTest {

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void testSerializeDate() throws Exception {

    String json = objectMapper.writeValueAsString(LocalDateTime.now().atOffset(ZoneOffset.UTC));
    // String json = objectMapper.writeValueAsString(LocalDateTime.of(2025, 6, 15, 21, 31, 45));
    // String json = objectMapper.writeValueAsString(LocalDateTime.now());
    assertThat(json).isEqualTo("\"2025-06-15T21:31:45\"");
  }
}
