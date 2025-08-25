package fr.revoicechat;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ReVoiceChatApplicationTests {

  @Test
  @SuppressWarnings("java:S1186")
  void contextLoads() {
    try {
      Assertions.assertThatCode(() -> ReVoiceChatApplication.main(new String[0])).doesNotThrowAnyException();
    } finally {
      if (ReVoiceChatApplication.CONTEXT != null) {
        Assertions.assertThatCode(() -> ReVoiceChatApplication.CONTEXT.close()).doesNotThrowAnyException();
      }
    }
  }
}
