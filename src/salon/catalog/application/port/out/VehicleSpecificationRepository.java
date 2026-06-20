package salon.catalog.application.port.out;

import salon.catalog.application.domain.model.specification.SpecificationId;
import salon.catalog.application.domain.model.specification.VehicleSpecification;

import java.util.Optional;

/**
 * PORT WYJŚCIOWY – repozytorium agregatu VehicleSpecification
 * („SpecificationDatabaseRepository”). Utrwala koszyki konfiguracyjne (również
 * wersje robocze – UC-KON-01 / A2).
 */
public interface VehicleSpecificationRepository {

    void save(VehicleSpecification specification);

    Optional<VehicleSpecification> findById(SpecificationId id);
}
