package tk.project.bankcards.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
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
import tk.project.bankcards.enums.BankCardStatus;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@Table(name = "bank_cards")
@RequiredArgsConstructor
@EqualsAndHashCode(of = "number")
@EntityListeners(AuditingEntityListener.class)
public class BankCardEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "number", updatable = false, nullable = false, unique = true, length = 16)
  private Long number;

  @ManyToOne
  @JoinColumn(name = "owner_id", updatable = false, nullable = false)
  private UserEntity owner;

  @Column(name = "expiry_date", nullable = false)
  private LocalDate expiryDate;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private BankCardStatus status;

  @Column(name = "balance", nullable = false, precision = 20, scale = 10)
  private BigDecimal balance;

  @CreatedDate
  @Column(name = "create_date_time", updatable = false, nullable = false)
  private Instant createDateTime;

  @LastModifiedDate
  @Column(name = "update_date_time", nullable = false)
  private Instant updateDateTime;
}
