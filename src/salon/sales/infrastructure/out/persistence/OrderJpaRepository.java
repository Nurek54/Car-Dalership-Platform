package salon.sales.infrastructure.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/** Spring Data JPA repository backing the {@link OrderDatabaseAdapter}. */
public interface OrderJpaRepository extends JpaRepository<OrderEntity, String> {

    Optional<OrderEntity> findBySourceOfferId(String sourceOfferId);
}
