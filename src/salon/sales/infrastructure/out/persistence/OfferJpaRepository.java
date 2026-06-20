package salon.sales.infrastructure.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OfferJpaRepository extends JpaRepository<OfferJpaEntity, String> {
}
