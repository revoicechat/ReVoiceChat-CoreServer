package fr.revoicechat;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

@SpringBootApplication
public class ReVoiceChatApplication {

  static ConfigurableApplicationContext CONTEXT;

  public static void main(String[] args) {
    CONTEXT = SpringApplication.run(ReVoiceChatApplication.class, args);
  }
}
