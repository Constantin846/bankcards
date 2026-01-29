package tk.project.bankcards.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tk.project.bankcards.entity.BankCardEntity;

@Repository
public interface BankCardRepository extends JpaRepository<BankCardEntity, UUID> {

  Optional<BankCardEntity> findByNumber(Long number);

  @Query(
      value =
          """
          SELECT *
          FROM bank_cards bc
          WHERE bc.id = :cardId
          FOR UPDATE
          """,
      nativeQuery = true)
  Optional<BankCardEntity> findByIdForUpdate(@Param("cardId") UUID cardId);

  List<BankCardEntity> findAllByOwnerId(UUID ownerId, Pageable pageable);
}
