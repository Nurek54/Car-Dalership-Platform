package salon.catalog.application.service;

import salon.catalog.application.port.in.UpdateCatalog;
import salon.catalog.application.port.out.CatalogImporterPort;
import salon.catalog.application.dto.ImportedCatalogData;
import salon.catalog.application.port.out.EventPublisher;
import salon.catalog.application.port.out.ProductCatalogRepository;
import salon.catalog.application.domain.exception.CatalogValidationException;
import salon.catalog.application.domain.model.catalog.ProductCatalogFactory;
import salon.catalog.application.domain.model.catalog.ProductCatalog;
import salon.catalog.application.domain.model.event.CatalogUpdateFailed;
import salon.catalog.application.domain.model.event.CatalogUpdated;
import salon.catalog.application.domain.service.RuleValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

/**
 * APPLICATION SERVICE (use-case facade for UC-KON-02) – implementation of the inbound
 * port {@link UpdateCatalog}.
 *
 * Orchestration: fetch the package via the ACL → build the aggregate with the factory → logical
 * validation (domain service) → archive the current and save the new catalog in
 * a single transaction → emit CatalogUpdated. On a validation/translation error
 * (scenariusz A1) odrzucenie pakietu i emisja CatalogUpdateFailed.
 */
@Service
public class UpdateCatalogService implements UpdateCatalog {

    private static final Logger log = LoggerFactory.getLogger(UpdateCatalogService.class);

    private final CatalogImporterPort catalogImporter;
    private final ProductCatalogRepository catalogRepository;
    private final RuleValidationService ruleValidationService;
    private final ProductCatalogFactory catalogFactory;
    private final EventPublisher eventPublisher;
    private final Clock clock;

    public UpdateCatalogService(CatalogImporterPort catalogImporter,
                                ProductCatalogRepository catalogRepository,
                                RuleValidationService ruleValidationService,
                                ProductCatalogFactory catalogFactory,
                                EventPublisher eventPublisher,
                                Clock clock) {
        this.catalogImporter = catalogImporter;
        this.catalogRepository = catalogRepository;
        this.ruleValidationService = ruleValidationService;
        this.catalogFactory = catalogFactory;
        this.eventPublisher = eventPublisher;
        this.clock = clock;
    }

    @Override
    @Transactional
    public void update() {
        try {
            // Steps 1–2: fetching and translating the package (the ACL performs the format translation).
            ImportedCatalogData data = catalogImporter.fetchLatestCatalog();

            // Step 4 (preparation): determining the next version relative to the current active catalog.
            Optional<ProductCatalog> current = catalogRepository.findActiveByModelYear(data.modelYear());
            int nextVersion = current.map(c -> c.version() + 1).orElse(1);

            // Building the aggregate with the factory – structural validation (prices, uniqueness, existence of options).
            ProductCatalog newCatalog = catalogFactory.createNextVersion(
                    data.modelYear(), nextVersion, data.options(), data.rules());

            // Step 3: logical validation of rule consistency (domain service).
            ruleValidationService.validateCatalogConsistency(newCatalog);

            // Step 4: saving the new catalog; the previous one is marked as archived.
            current.ifPresent(c -> {
                c.archive();
                catalogRepository.save(c);
            });
            catalogRepository.save(newCatalog);

            // Step 5: emitting the event on the data bus.
            eventPublisher.publish(new CatalogUpdated(
                    newCatalog.id(), newCatalog.modelYear(), newCatalog.version(), Instant.now(clock)));

            log.info("Catalog updated: {} (model year {}, version {})",
                    newCatalog.id(), newCatalog.modelYear(), newCatalog.version());

        } catch (CatalogValidationException | IllegalArgumentException e) {
            // Scenario A1: critical error – the package is rejected, logged for IT support.
            log.error("Catalog update aborted: {}", e.getMessage(), e);
            eventPublisher.publish(new CatalogUpdateFailed(e.getMessage(), Instant.now(clock)));
        }
    }
}
