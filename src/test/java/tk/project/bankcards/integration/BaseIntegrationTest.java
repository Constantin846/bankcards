package tk.project.bankcards.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import tk.project.bankcards.config.EndpointsConfig;
import tk.project.bankcards.entity.UserEntity;
import tk.project.bankcards.enums.Role;
import tk.project.bankcards.repository.BankCardRepository;
import tk.project.bankcards.repository.RequestRepository;
import tk.project.bankcards.repository.UserRepository;

@AutoConfigureMockMvc
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
      "spring.datasource.url=jdbc:h2:mem:db;LOCK_TIMEOUT=60000",
      "spring.jpa.hibernate.ddl-auto=create-drop",
      "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
      "spring.datasource.driverClassName: org.h2.Driver",
      "spring.liquibase.enabled=false",
      "app.admin-init.enabled=false"
    })
class BaseIntegrationTest {

  @Autowired protected PasswordEncoder passwordEncoder;
  @Autowired protected EndpointsConfig endpointsConfig;
  @Autowired protected ObjectMapper objectMapper;
  @Autowired protected MockMvc mockMvc;
  @Autowired protected BankCardRepository bankCardRepository;
  @Autowired protected RequestRepository requestRepository;
  @Autowired protected UserRepository userRepository;

  @AfterEach
  void clearDatabase() {
    requestRepository.deleteAll();
    bankCardRepository.deleteAll();
    userRepository.deleteAll();
  }

  protected UserEntity admin;
  protected String adminPassword;

  @BeforeEach
  void saveAdmin() {
    adminPassword = "admin pass";
    admin =
        tk.project.bankcards.entity.UserEntity.builder()
            .name("admin name")
            .password(passwordEncoder.encode(adminPassword))
            .email("admin_email")
            .role(Role.ADMIN)
            .build();
    userRepository.save(admin);
  }

  protected UserEntity existingUser;
  protected String existingUserPassword;

  protected void saveExistingUser() {
    existingUserPassword = "existingUserPassword";
    existingUser =
        UserEntity.builder()
            .name("existing name")
            .password(passwordEncoder.encode(existingUserPassword))
            .email("existing_email@mail.em")
            .role(Role.USER)
            .build();
    userRepository.save(existingUser);
  }
}
