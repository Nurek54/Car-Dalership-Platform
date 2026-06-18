package salon.catalog.application.port.out;

import salon.catalog.application.domain.model.specification.VehicleSpecification;
import salon.common.model.SpecificationId;

import java.util.Optional;

public interface SpecificationDatabaseRepository {
    void save(VehicleSpecification specification);
    Optional<VehicleSpecification> findById(SpecificationId id);
}
