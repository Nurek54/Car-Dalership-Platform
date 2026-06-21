package salon.catalog.application.port.out;

import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.catalog.ProductCatalog;

import java.util.Optional;

/**
 * OUTBOUND PORT – repository of the ProductCatalog aggregate ("CatalogDatabaseRepository").
 *
 * Persists and globally provides aggregates of one type; it creates the illusion
 * of keeping all catalogs in memory and decouples the application from
 * the storage technology. The repository does NOT control transactions and does NOT create aggregates.
 *
 * The port definition (abstraction) belongs to the application layer; it is implemented by an adapter
 * in the infrastructure layer (the Dependency Inversion Principle – the D in SOLID).
 */
public interface ProductCatalogRepository {

    void save(ProductCatalog catalog);

    Optional<ProductCatalog> findById(CatalogId id);

    /** The active catalog for a given model year (the price source in UC-KON-01). */
    Optional<ProductCatalog> findActiveByModelYear(ModelYear modelYear);
}
