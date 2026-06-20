package salon.billing.infrastructure.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementJpaRepository extends JpaRepository<SettlementJpaEntity, String> {
    Optional<SettlementJpaEntity> findByOrderId(String orderId);
}
