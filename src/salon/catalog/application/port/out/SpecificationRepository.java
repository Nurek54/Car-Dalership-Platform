package salon.catalog.application.port.out;

import salon.catalog.domain.model.specification.VehicleSpecification;
import salon.shared.model.SpecificationId;

import java.util.Optional;

public interface SpecificationRepository {
    void save(VehicleSpecification specification);
    Optional<VehicleSpecification> findById(SpecificationId id);
}
