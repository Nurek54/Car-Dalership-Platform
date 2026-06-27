package salon.catalog.application.port.out;

import salon.catalog.application.domain.model.specification.SpecificationId;
import salon.catalog.application.domain.model.specification.VehicleSpecification;

import java.util.Optional;

public interface VehicleSpecificationRepository {

    void save(VehicleSpecification specification);

    Optional<VehicleSpecification> findById(SpecificationId id);
}
