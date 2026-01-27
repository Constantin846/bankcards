package tk.project.bankcards.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import tk.project.bankcards.enums.Role;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

  private final EndpointsConfig endpointsConfig;

  @Bean
  public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http.csrf(CsrfConfigurer::disable)
        .httpBasic(Customizer.withDefaults())
        .headers(headers -> headers.frameOptions(HeadersConfigurer.FrameOptionsConfig::disable))
        .authorizeHttpRequests(
            auth ->
                auth.requestMatchers(
                        endpointsConfig
                            .getAccessRules()
                            .getPublicEndpoints()
                            .toArray(String[]::new))
                    .permitAll()
                    .requestMatchers(
                        endpointsConfig.getAccessRules().getUserEndpoints().toArray(String[]::new))
                    .hasAuthority(Role.USER.getAuthority())
                    .requestMatchers(
                        endpointsConfig.getAccessRules().getAdminEndpoints().toArray(String[]::new))
                    .hasAuthority(Role.ADMIN.getAuthority())
                    .anyRequest()
                    .authenticated())
        .logout(
            logout ->
                logout.logoutSuccessHandler(
                    new HttpStatusReturningLogoutSuccessHandler(HttpStatus.UNAUTHORIZED)))
        .build();
  }
}
