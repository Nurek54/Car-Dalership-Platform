package salon.catalog.infrastructure.out.mock;

import salon.catalog.application.port.out.SpecificationDatabaseRepository;
import salon.catalog.application.domain.model.specification.VehicleSpecification;
import salon.common.model.SpecificationId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemorySpecificationRepository implements SpecificationDatabaseRepository {

    private final Map<SpecificationId, VehicleSpecification> store = new HashMap<>();

    @Override
    public void save(VehicleSpecification specification) {
        this.store.put(specification.getId(), specification);
    }

    @Override
    public Optional<VehicleSpecification> findById(SpecificationId id) {
        return Optional.ofNullable(this.store.get(id));
    }
}
