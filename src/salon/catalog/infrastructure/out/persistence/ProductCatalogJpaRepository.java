package salon.catalog.infrastructure.out.persistence;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductCatalogJpaRepository extends JpaRepository<ProductCatalogJpaEntity, String> {
}
