package salon.catalog.infrastructure.out.persistence;

import java.util.List;
import java.util.Optional;

/**
 * Technical boundary of the persistent storage ("DBAdapter" from the diagram) – a thin contract
 * to the actual database driver.
 *
 * The repository adapters (SpecificationDatabaseRepository, CatalogDatabaseRepository)
 * translate the domain model into persistent-storage records and delegate to this interface.
 * The concrete implementation (JDBC/JPA/NoSQL) lies OUTSIDE the Catalog context – it is
 * an external infrastructure detail, which is why it appears here only as a port.
 *
 * A "persistent store" style repository (like a HashMap): {@link #upsert} writes/overwrites.
 */
public interface DbAdapter {

    void upsert(String collection, String id, Object record);

    Optional<Object> findById(String collection, String id);

    List<Object> findAll(String collection);
}
