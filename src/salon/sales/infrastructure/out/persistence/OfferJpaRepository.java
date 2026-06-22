package salon.sales.infrastructure.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

/** Spring Data JPA repository backing the {@link OfferDatabaseAdapter}. */
public interface OfferJpaRepository extends JpaRepository<OfferEntity, String> {
}
