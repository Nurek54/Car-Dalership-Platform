package salon.catalog.infrastructure.mock;

import salon.catalog.application.port.out.SpecificationRepository;
import salon.catalog.domain.model.specification.VehicleSpecification;
import salon.shared.model.SpecificationId;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemorySpecificationRepository implements SpecificationRepository {

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
