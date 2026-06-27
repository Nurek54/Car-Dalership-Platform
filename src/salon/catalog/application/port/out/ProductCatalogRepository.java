package salon.catalog.application.port.out;

import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.catalog.ProductCatalog;

import java.util.Optional;

public interface ProductCatalogRepository {

    void save(ProductCatalog catalog);

    Optional<ProductCatalog> findById(CatalogId id);

    
    Optional<ProductCatalog> findActiveByModelYear(ModelYear modelYear);
}
