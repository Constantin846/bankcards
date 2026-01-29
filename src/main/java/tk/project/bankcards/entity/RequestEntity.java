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
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import tk.project.bankcards.enums.RequestAction;
import tk.project.bankcards.enums.RequestStatus;

@Entity
@Getter
@Setter
@Builder
@AllArgsConstructor
@Table(name = "requests")
@RequiredArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class RequestEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne
  @JoinColumn(name = "owner_id", updatable = false, nullable = false)
  private UserEntity owner;

  @Column(name = "bank_card_id", updatable = false, nullable = false)
  private UUID bankCardId;

  @Enumerated(EnumType.STRING)
  @Column(name = "action", updatable = false, nullable = false)
  private RequestAction action;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private RequestStatus status;

  @CreatedDate
  @Column(name = "create_date_time", updatable = false, nullable = false)
  private Instant createDateTime;

  @LastModifiedDate
  @Column(name = "update_date_time", nullable = false)
  private Instant updateDateTime;
}
