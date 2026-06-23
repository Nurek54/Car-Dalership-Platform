package salon.catalog.infrastructure.out.persistence;

import salon.catalog.application.port.out.ProductCatalogRepository;
import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.domain.model.catalog.CatalogState;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.catalog.ProductCatalog;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class CatalogDatabaseRepository implements ProductCatalogRepository {

    private static final String COLLECTION = "product_catalog";

    private final DbAdapter dbAdapter;
    private final ProductCatalogPersistenceMapper mapper;

    public CatalogDatabaseRepository(DbAdapter dbAdapter, ProductCatalogPersistenceMapper mapper) {
        this.dbAdapter = dbAdapter;
        this.mapper = mapper;
    }

    @Override
    public void save(ProductCatalog catalog) {
        dbAdapter.upsert(COLLECTION, catalog.id().toString(), mapper.toRecord(catalog));
    }

    @Override
    public Optional<ProductCatalog> findById(CatalogId id) {
        return dbAdapter.findById(COLLECTION, id.toString())
                .map(r -> mapper.toDomain((ProductCatalogRecord) r));
    }

    @Override
    public Optional<ProductCatalog> findActiveByModelYear(ModelYear modelYear) {
        return dbAdapter.findAll(COLLECTION).stream()
                .map(r -> mapper.toDomain((ProductCatalogRecord) r))
                .filter(c -> c.state() == CatalogState.ACTIVE && c.modelYear().equals(modelYear))
                .findFirst();
    }
}
