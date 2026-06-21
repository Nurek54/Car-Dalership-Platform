package salon.catalog.infrastructure.out.persistence;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A concrete implementation of the technical port {@link DbAdapter} — persistent storage
 * in memory (HashMap-like behavior, per the contract description).
 *
 * This is the only "database driver" required for the Catalog context to run standalone
 * (the repository adapters delegate here). In a production environment this bean can be
 * replaced with a JDBC/JPA/NoSQL variant without changes in the application layer or the domain —
 * that is the point of dependency inversion (the port in the application, the detail in the infrastructure).
 */
@Repository
public class InMemoryDbAdapter implements DbAdapter {

    private final Map<String, Map<String, Object>> store = new ConcurrentHashMap<>();

    @Override
    public void upsert(String collection, String id, Object record) {
        store.computeIfAbsent(collection, k -> new ConcurrentHashMap<>()).put(id, record);
    }

    @Override
    public Optional<Object> findById(String collection, String id) {
        return Optional.ofNullable(store.getOrDefault(collection, Map.of()).get(id));
    }

    @Override
    public List<Object> findAll(String collection) {
        return new ArrayList<>(store.getOrDefault(collection, Map.of()).values());
    }
}
