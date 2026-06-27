package salon.catalog.infrastructure.out.persistence;

import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

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
