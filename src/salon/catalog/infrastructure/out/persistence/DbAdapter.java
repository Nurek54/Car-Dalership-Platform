package salon.catalog.infrastructure.out.persistence;

import java.util.List;
import java.util.Optional;

public interface DbAdapter {

    void upsert(String collection, String id, Object record);

    Optional<Object> findById(String collection, String id);

    List<Object> findAll(String collection);
}
