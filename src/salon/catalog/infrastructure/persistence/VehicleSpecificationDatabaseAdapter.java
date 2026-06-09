package salon.catalog.infrastructure.persistence;

import salon.catalog.infrastructure.persistence.VehicleSpecificationJpaEntity;
import salon.catalog.infrastructure.persistence.VehicleSpecificationJpaRepository;

import salon.shared.infrastructure.persistence.DomainReflection;
import org.springframework.stereotype.Component;
import salon.catalog.application.port.out.SpecificationRepository;
import salon.catalog.domain.model.catalog.CatalogId;
import salon.catalog.domain.model.catalog.OptionCode;
import salon.catalog.domain.model.specification.SpecificationState;
import salon.catalog.domain.model.specification.VehicleSpecification;
import salon.shared.model.Money;
import salon.shared.model.SpecificationId;

import java.util.List;
import java.util.Optional;

/**
 * Adapter sterowany (driven) — persystencja specyfikacji pojazdu (port {@link SpecificationRepository}).
 *
 * Agregat waliduje dodanie opcji względem cennika (którego adapter nie posiada przy odczycie),
 * dlatego wybrane opcje/stan/cenę odtwarzamy refleksją w infrastrukturze — domena pozostaje czysta.
 */
@Component
public class VehicleSpecificationDatabaseAdapter implements SpecificationRepository {

    private final VehicleSpecificationJpaRepository repository;

    public VehicleSpecificationDatabaseAdapter(VehicleSpecificationJpaRepository repository) {
        this.repository = repository;
    }

    @Override
    public void save(VehicleSpecification specification) {
        repository.save(toEntity(specification));
    }

    @Override
    public Optional<VehicleSpecification> findById(SpecificationId id) {
        return repository.findById(id.value()).map(this::toDomain);
    }

    private VehicleSpecificationJpaEntity toEntity(VehicleSpecification specification) {
        VehicleSpecificationJpaEntity entity = new VehicleSpecificationJpaEntity();
        entity.id = specification.getId().value();
        entity.catalogId = specification.getCatalogId().value();
        entity.state = specification.getState().name();
        if (specification.getTotalPrice() != null) {
            entity.totalPrice = specification.getTotalPrice().amount();
            entity.currency = specification.getTotalPrice().currency();
        }
        List<OptionCode> options = specification.getSelectedOptions();
        for (int i = 0; i < options.size(); i++) {
            entity.options.add(options.get(i).value());
        }
        return entity;
    }

    @SuppressWarnings("unchecked")
    private VehicleSpecification toDomain(VehicleSpecificationJpaEntity entity) {
        VehicleSpecification specification = new VehicleSpecification(
                new SpecificationId(entity.id), new CatalogId(entity.catalogId));
        List<OptionCode> picked = (List<OptionCode>) DomainReflection.get(specification, "optionsPicked");
        for (int i = 0; i < entity.options.size(); i++) {
            picked.add(new OptionCode(entity.options.get(i)));
        }
        if (entity.totalPrice != null) {
            DomainReflection.set(specification, "totalPrice", Money.of(entity.totalPrice, entity.currency));
        }
        DomainReflection.set(specification, "state", SpecificationState.valueOf(entity.state));
        return specification;
    }
}
