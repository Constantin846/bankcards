package tk.project.bankcards;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@EnableWebSecurity
@EnableJpaAuditing
@SpringBootApplication
public class BankcardsApplication {

  public static void main(String[] args) {
    SpringApplication.run(BankcardsApplication.class);
  }
}
