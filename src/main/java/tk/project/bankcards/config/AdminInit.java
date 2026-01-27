package tk.project.bankcards.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import tk.project.bankcards.entity.UserEntity;
import tk.project.bankcards.enums.Role;
import tk.project.bankcards.repository.UserRepository;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "app.admin-init", name = "enabled", matchIfMissing = true)
public class AdminInit {

  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;

  @PostConstruct
  public void registerAdmin() {
    String email = "admin";

    if (userRepository.findByEmail(email).isPresent()) {
      log.info("Админ уже был зарегистрирован");
      return;
    }

    UserEntity admin =
        UserEntity.builder()
            .name("admin")
            .password(passwordEncoder.encode("admin"))
            .email(email)
            .role(Role.ADMIN)
            .build();

    userRepository.save(admin);
    log.info("Админ успешно зарегистрирован");
  }
}
