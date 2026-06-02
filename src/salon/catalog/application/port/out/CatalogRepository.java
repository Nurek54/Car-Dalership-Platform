package salon.catalog.application.port.out;

import salon.catalog.domain.model.catalog.CatalogId;
import salon.catalog.domain.model.catalog.ProductCatalog;

import java.util.Optional;

public interface CatalogRepository {
    void save(ProductCatalog catalog);
    Optional<ProductCatalog> findById(CatalogId id);
}
