package salon.sales.infrastructure.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OrderJpaRepository extends JpaRepository<OrderJpaEntity, String> {

    Optional<OrderJpaEntity> findBySourceOfferId(String sourceOfferId);
}
