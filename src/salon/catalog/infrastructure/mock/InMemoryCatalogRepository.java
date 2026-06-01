package salon.catalog.infrastructure.mock;

import salon.catalog.application.port.out.CatalogRepository;
import salon.catalog.domain.model.catalog.CatalogId;
import salon.catalog.domain.model.catalog.ProductCatalog;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryCatalogRepository implements CatalogRepository {

    private final Map<CatalogId, ProductCatalog> store = new HashMap<>();

    @Override
    public void save(ProductCatalog catalog) {
        this.store.put(catalog.getCatalogId(), catalog);
    }

    @Override
    public Optional<ProductCatalog> findById(CatalogId id) {
        return Optional.ofNullable(this.store.get(id));
    }
}
