package salon.catalog.application.service;

import salon.catalog.application.port.in.BuildSpecificationUseCase;
import salon.catalog.application.port.out.SpecificationRepository;
import salon.catalog.domain.model.catalog.CatalogId;
import salon.catalog.domain.model.catalog.OptionCode;
import salon.catalog.domain.model.specification.VehicleSpecification;
import salon.catalog.domain.service.RuleValidationDomainService;
import salon.shared.application.EventPublisherPort;
import salon.shared.event.DomainEvent;
import salon.shared.model.SpecificationId;

import java.util.List;
import java.util.Optional;

/**
 * Realizuje UC-KAT-01 (orkiestracja). Zgodnie z diagramem architektury serwis aplikacyjny zależy od
 * SpecificationRepository, RuleValidationDomainService i EventPublisherPort. To serwis dziedzinowy
 * (RuleValidationDomainService) czyta cennik z CatalogRepository — aplikacja pozostaje cienka.
 */
public class SpecificationAppService implements BuildSpecificationUseCase {

    private final SpecificationRepository specificationRepository;
    private final RuleValidationDomainService ruleValidation;
    private final EventPublisherPort eventPublisher;

    public SpecificationAppService(SpecificationRepository specificationRepository,
                                   RuleValidationDomainService ruleValidation,
                                   EventPublisherPort eventPublisher) {
        if (specificationRepository == null) {
            throw new IllegalArgumentException("specificationRepository must not be null.");
        }
        if (ruleValidation == null) {
            throw new IllegalArgumentException("ruleValidation must not be null.");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        this.specificationRepository = specificationRepository;
        this.ruleValidation = ruleValidation;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public SpecificationId startSpecification(String catalogId) {
        VehicleSpecification specification =
                new VehicleSpecification(SpecificationId.generate(), new CatalogId(catalogId));
        specificationRepository.save(specification);
        return specification.getId();
    }

    @Override
    public void addOption(String specificationId, String catalogId, String optionCode) {
        VehicleSpecification specification = loadSpecification(specificationId);
        // Serwis dziedzinowy sam pobiera cennik z repozytorium i waliduje reguły (Fail-fast).
        ruleValidation.validateAndAddOption(specification, new OptionCode(optionCode));
        specificationRepository.save(specification);
    }

    @Override
    public void finalizeSpecification(String specificationId) {
        VehicleSpecification specification = loadSpecification(specificationId);
        specification.finalizeSpecification();
        specificationRepository.save(specification);
        publishEventsOf(specification);
    }

    private VehicleSpecification loadSpecification(String specificationId) {
        Optional<VehicleSpecification> found =
                specificationRepository.findById(new SpecificationId(specificationId));
        if (found.isEmpty()) {
            throw new IllegalStateException("Specification not found: " + specificationId);
        }
        return found.get();
    }

    private void publishEventsOf(VehicleSpecification specification) {
        List<DomainEvent> events = specification.pullDomainEvents();
        for (int i = 0; i < events.size(); i++) {
            eventPublisher.publish(events.get(i));
        }
    }
}
