package tk.project.bankcards.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tk.project.bankcards.entity.RequestEntity;

@Repository
public interface RequestRepository extends JpaRepository<RequestEntity, UUID> {}
