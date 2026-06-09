package salon.catalog.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCatalogJpaRepository extends JpaRepository<ProductCatalogJpaEntity, String> {
}
