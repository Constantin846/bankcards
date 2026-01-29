package tk.project.bankcards.config;

import java.util.List;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "app.controller")
public class EndpointsConfig {

  private String basePath;
  private String h2Path;
  private String registrationPath;
  private String usersPath;
  private String bankCardsPath;
  private String requestsPath;
  private String adminAccess;
  private String userAccess;

  private AccessRules accessRules = new AccessRules();

  @Data
  public static class AccessRules {
    private List<String> publicEndpoints;
    private List<String> userEndpoints;
    private List<String> adminEndpoints;
  }
}
