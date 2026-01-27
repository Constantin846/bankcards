package tk.project.bankcards.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import tk.project.bankcards.enums.Role;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@Table(name = "users")
@RequiredArgsConstructor
@EqualsAndHashCode(of = "email")
@EntityListeners(AuditingEntityListener.class)
public class UserEntity implements UserDetails {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = 20)
  private String name;

  @Column(nullable = false, length = 60)
  private String password;

  @Column(nullable = false, unique = true, length = 30)
  private String email;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Role role;

  @CreatedDate
  @Column(name = "create_date_time", updatable = false, nullable = false)
  private Instant createDateTime;

  @LastModifiedDate
  @Column(name = "update_date_time", nullable = false)
  private Instant updateDateTime;

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    GrantedAuthority authority = new SimpleGrantedAuthority(this.getRole().getAuthority());
    return Set.of(authority);
  }

  @Override
  public String getUsername() {
    return this.getEmail();
  }
}
