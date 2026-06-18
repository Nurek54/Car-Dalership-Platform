package salon.catalog.application.service;

import salon.catalog.application.port.in.BuildSpecification;
import salon.catalog.application.port.out.SpecificationDatabaseRepository;
import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.domain.model.catalog.OptionCode;
import salon.catalog.application.domain.model.specification.VehicleSpecification;
import salon.catalog.application.domain.service.RuleValidationService;
import salon.common.application.EventPublisher;
import salon.common.event.DomainEvent;
import salon.common.model.SpecificationId;

import java.util.List;
import java.util.Optional;

/**
 * Realizuje UC-KON-01 (orkiestracja). Zgodnie z diagramem architektury serwis aplikacyjny zależy od
 * SpecificationDatabaseRepository, RuleValidationService i EventPublisher. To serwis dziedzinowy
 * (RuleValidationService) czyta cennik z CatalogDatabaseRepository — aplikacja pozostaje cienka.
 */
public class BuildSpecificationService implements BuildSpecification {

    private final SpecificationDatabaseRepository specificationRepository;
    private final RuleValidationService ruleValidation;
    private final EventPublisher eventPublisher;

    public BuildSpecificationService(SpecificationDatabaseRepository specificationRepository,
                                   RuleValidationService ruleValidation,
                                   EventPublisher eventPublisher) {
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
        // Reguła finalizacji (UC-KON-01): serwis dziedzinowy ocenia kompletność (REQUIRES) wg cennika.
        ruleValidation.assertComplete(specification);
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
