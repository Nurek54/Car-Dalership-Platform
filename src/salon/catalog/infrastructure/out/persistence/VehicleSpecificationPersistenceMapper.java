package salon.catalog.infrastructure.out.persistence;

import salon.catalog.application.domain.model.specification.VehicleSpecificationFactory;
import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;
import salon.catalog.application.domain.model.specification.SpecificationId;
import salon.catalog.application.domain.model.specification.SpecificationState;
import salon.catalog.application.domain.model.specification.VehicleSpecification;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class VehicleSpecificationPersistenceMapper {

    private final VehicleSpecificationFactory factory;

    public VehicleSpecificationPersistenceMapper(VehicleSpecificationFactory factory) {
        this.factory = factory;
    }

    public VehicleSpecificationRecord toRecord(VehicleSpecification specification) {
        return new VehicleSpecificationRecord(
                specification.id().toString(),
                specification.catalogId().toString(),
                specification.totalPrice().amount(),
                specification.totalPrice().currency().getCurrencyCode(),
                specification.state().name(),
                specification.optionsPicked().stream().map(OptionCode::value).toList());
    }

    public VehicleSpecification toDomain(VehicleSpecificationRecord record) {
        Set<OptionCode> options = record.optionsPicked().stream()
                .map(OptionCode::of)
                .collect(Collectors.toCollection(LinkedHashSet::new));

        return factory.reconstitute(
                SpecificationId.of(record.id()),
                CatalogId.of(record.catalogId()),
                Money.of(record.totalPrice(), record.currency()),
                SpecificationState.valueOf(record.state()),
                options);
    }
}
