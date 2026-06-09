package salon.sales.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferJpaRepository extends JpaRepository<OfferJpaEntity, String> {
}
