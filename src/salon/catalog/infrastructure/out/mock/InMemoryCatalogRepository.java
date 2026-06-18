package salon.catalog.infrastructure.out.mock;

import salon.catalog.application.port.out.CatalogDatabaseRepository;
import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.domain.model.catalog.ProductCatalog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryCatalogRepository implements CatalogDatabaseRepository {

    private final Map<CatalogId, ProductCatalog> store = new HashMap<>();

    @Override
    public void save(ProductCatalog catalog) {
        this.store.put(catalog.getCatalogId(), catalog);
    }

    @Override
    public Optional<ProductCatalog> findById(CatalogId id) {
        return Optional.ofNullable(this.store.get(id));
    }

    @Override
    public List<ProductCatalog> findAll() {
        return new ArrayList<>(this.store.values());
    }
}
