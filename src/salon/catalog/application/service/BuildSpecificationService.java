package salon.catalog.application.service;

import salon.catalog.application.command.AddOptionCommand;
import salon.catalog.application.command.FinalizeSpecificationCommand;
import salon.catalog.application.command.InitiateConfiguratorSessionCommand;
import salon.catalog.application.command.RemoveOptionCommand;
import salon.catalog.application.dto.SpecificationView;
import salon.catalog.application.port.in.BuildSpecification;
import salon.catalog.application.port.out.EventPublisher;
import salon.catalog.application.port.out.ProductCatalogRepository;
import salon.catalog.application.port.out.VehicleSpecificationRepository;
import salon.catalog.application.domain.exception.CatalogNotFoundException;
import salon.catalog.application.domain.exception.SpecificationNotFoundException;
import salon.catalog.application.domain.model.specification.VehicleSpecificationFactory;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.catalog.ProductCatalog;
import salon.catalog.application.domain.model.event.SpecificationCompleted;
import salon.catalog.application.domain.model.shared.OptionCode;
import salon.catalog.application.domain.model.specification.SpecificationId;
import salon.catalog.application.domain.model.specification.VehicleSpecification;
import salon.catalog.application.domain.service.RuleValidationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

/**
 * APPLICATION SERVICE (use-case facade for UC-KON-01) – implementation of the inbound
 * port {@link BuildSpecification}.
 *
 * Performs NON-BUSINESS operations: use-case orchestration, management
 * of persistence transactions and handling of domain events. The business logic (rules,
 * invariants, prices) is delegated to the aggregates and the domain service.
 */
@Service
public class BuildSpecificationService implements BuildSpecification {

    private final ProductCatalogRepository catalogRepository;
    private final VehicleSpecificationRepository specificationRepository;
    private final RuleValidationService ruleValidationService;
    private final VehicleSpecificationFactory specificationFactory;
    private final EventPublisher eventPublisher;
    private final Clock clock;

    public BuildSpecificationService(ProductCatalogRepository catalogRepository,
                                     VehicleSpecificationRepository specificationRepository,
                                     RuleValidationService ruleValidationService,
                                     VehicleSpecificationFactory specificationFactory,
                                     EventPublisher eventPublisher,
                                     Clock clock) {
        this.catalogRepository = catalogRepository;
        this.specificationRepository = specificationRepository;
        this.ruleValidationService = ruleValidationService;
        this.specificationFactory = specificationFactory;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    /** Step 1: opening a configurator session and creating a working specification. */
    @Override
    @Transactional
    public SpecificationView initiate(InitiateConfiguratorSessionCommand command) {
        ProductCatalog catalog = catalogRepository
                .findActiveByModelYear(ModelYear.of(command.modelYear()))
                .orElseThrow(() -> new CatalogNotFoundException(
                        "No active catalog for model year " + command.modelYear()));

        VehicleSpecification specification =
                specificationFactory.createDraft(catalog.id(), catalog.currencyCode());
        specificationRepository.save(specification);
        return SpecificationView.from(specification);
    }

    /** Steps 2–4: adding an option + on-the-fly verification of exclusion rules (A1). */
    @Override
    @Transactional
    public SpecificationView addOption(AddOptionCommand command) {
        VehicleSpecification specification = loadSpecification(command.specificationId());
        ProductCatalog catalog = loadCatalogFor(specification);

        specification.addOption(OptionCode.of(command.optionCode()), catalog);
        // Validation of the whole state after adding an option – if the combination is blocked,
        // the exception aborts the transaction and the specification is not saved.
        ruleValidationService.validateSelection(specification, catalog);

        specificationRepository.save(specification);
        return SpecificationView.from(specification);
    }

    @Override
    @Transactional
    public SpecificationView removeOption(RemoveOptionCommand command) {
        VehicleSpecification specification = loadSpecification(command.specificationId());
        ProductCatalog catalog = loadCatalogFor(specification);

        specification.removeOption(OptionCode.of(command.optionCode()), catalog);
        specificationRepository.save(specification);
        return SpecificationView.from(specification);
    }

    /** Steps 5–7: completeness verification, finalization and event emission. */
    @Override
    @Transactional
    public SpecificationView finalizeSpecification(FinalizeSpecificationCommand command) {
        VehicleSpecification specification = loadSpecification(command.specificationId());
        ProductCatalog catalog = loadCatalogFor(specification);

        ruleValidationService.validateComplete(specification, catalog);
        specification.finalizeSpecification();
        specificationRepository.save(specification);

        eventPublisher.publish(new SpecificationCompleted(
                specification.id(),
                specification.catalogId(),
                specification.totalPrice(),
                specification.optionsPicked(),
                Instant.now(clock)));

        return SpecificationView.from(specification);
    }

    private VehicleSpecification loadSpecification(String id) {
        SpecificationId specificationId = SpecificationId.of(id);
        return specificationRepository.findById(specificationId)
                .orElseThrow(() -> new SpecificationNotFoundException(specificationId));
    }

    private ProductCatalog loadCatalogFor(VehicleSpecification specification) {
        return catalogRepository.findById(specification.catalogId())
                .orElseThrow(() -> new CatalogNotFoundException(
                        "No catalog " + specification.catalogId()));
    }
}
