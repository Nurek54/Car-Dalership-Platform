package acceptance.catalog_context;

import org.junit.jupiter.api.Test;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;

import salon.catalog.application.port.in.BuildSpecificationUseCase;
import salon.catalog.application.port.in.UpdateCatalogUseCase;
import salon.catalog.application.port.out.CatalogRepository;
import salon.catalog.application.port.out.ImporterApiPort;
import salon.catalog.application.port.out.SpecificationRepository;
import salon.catalog.application.service.CatalogAppService;
import salon.catalog.application.service.SpecificationAppService;
import salon.catalog.domain.event.CatalogUpdateFailedEvent;
import salon.catalog.domain.event.CatalogUpdatedEvent;
import salon.catalog.domain.event.SpecificationCompletedEvent;
import salon.catalog.domain.model.catalog.CatalogId;
import salon.catalog.domain.model.catalog.CatalogOption;
import salon.catalog.domain.model.catalog.CatalogRule;
import salon.catalog.domain.model.catalog.CatalogState;
import salon.catalog.domain.model.catalog.OptionCode;
import salon.catalog.domain.model.catalog.ProductCatalog;
import salon.catalog.domain.model.catalog.RuleType;
import salon.catalog.domain.model.specification.RuleViolationException;
import salon.catalog.domain.model.specification.SpecificationState;
import salon.catalog.domain.model.specification.VehicleSpecification;
import salon.catalog.domain.service.RuleValidationDomainService;
import salon.shared.application.EventPublisherPort;
import salon.shared.model.Money;
import salon.shared.model.SpecificationId;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Acceptance tests of the Catalog and Configurator Context (UC-KON-01, UC-KON-02).
 *
 * The context exposes no REST adapters — per the sequence diagrams it is driven by:
 * the Customer's configurator (BuildSpecificationUseCase) and the Importer's webhook/cron
 * (UpdateCatalogUseCase). The tests therefore enter through the inbound ports (use cases),
 * and verify the postconditions where sales does: in the physical SQL database and on the event
 * bus (RabbitTemplate). The Importer (Blackbox) is stubbed at the ACL port level.
 */
@SpringBootTest
class CatalogContextAcceptanceTest {

    /** Composition root for the test: the Catalog services are pure POJOs (without @Service). */
    @TestConfiguration
    static class CatalogWiring {

        @Bean
        RuleValidationDomainService ruleValidationDomainService(CatalogRepository catalogRepository) {
            return new RuleValidationDomainService(catalogRepository);
        }

        @Bean
        UpdateCatalogUseCase updateCatalogUseCase(CatalogRepository catalogRepository,
                                                  ImporterApiPort importerApiPort,
                                                  EventPublisherPort eventPublisherPort) {
            return new CatalogAppService(catalogRepository, importerApiPort, eventPublisherPort);
        }

        @Bean
        BuildSpecificationUseCase buildSpecificationUseCase(SpecificationRepository specificationRepository,
                                                            RuleValidationDomainService ruleValidation,
                                                            EventPublisherPort eventPublisherPort) {
            return new SpecificationAppService(specificationRepository, ruleValidation, eventPublisherPort);
        }
    }

    @Autowired private UpdateCatalogUseCase updateCatalogUseCase;
    @Autowired private BuildSpecificationUseCase buildSpecificationUseCase;
    @Autowired private CatalogRepository catalogRepository;       // ProductCatalogDatabaseAdapter
    @Autowired private SpecificationRepository specificationRepository; // VehicleSpecificationDatabaseAdapter

    @MockBean private ImporterApiPort importerApi;   // The external manufacturer/importer system (Blackbox)
    @MockBean private RabbitTemplate rabbitTemplate; // The event bus

    // Price list per the UC-KON-01 documentation: packages (A1, A2), engines (B1, B2), gearboxes (C1, C2), color.
    // Manufacturer rule: engine B2 cannot be selected with gearbox C1 (EXCLUDES),
    // and engine B2 requires gearbox C2 (REQUIRES — completeness check in step 6).
    private ProductCatalog createPublishedCatalog() {
        ProductCatalog catalog = ProductCatalog.createActive("MY_2026");
        catalog.addOption(new CatalogOption(new OptionCode("PAKIET_A1"), Money.of(15000, "PLN")));
        catalog.addOption(new CatalogOption(new OptionCode("PAKIET_A2"), Money.of(22000, "PLN")));
        catalog.addOption(new CatalogOption(new OptionCode("SILNIK_B1"), Money.of(20000, "PLN")));
        catalog.addOption(new CatalogOption(new OptionCode("SILNIK_B2"), Money.of(28000, "PLN")));
        catalog.addOption(new CatalogOption(new OptionCode("SKRZYNIA_C1"), Money.of(8000, "PLN")));
        catalog.addOption(new CatalogOption(new OptionCode("SKRZYNIA_C2"), Money.of(12000, "PLN")));
        catalog.addOption(new CatalogOption(new OptionCode("KOLOR_CZARNY"), Money.of(3000, "PLN")));
        catalog.addRule(new CatalogRule(
                new OptionCode("SILNIK_B2"), new OptionCode("SKRZYNIA_C1"), RuleType.EXCLUDES));
        catalog.addRule(new CatalogRule(
                new OptionCode("SILNIK_B2"), new OptionCode("SKRZYNIA_C2"), RuleType.REQUIRES));
        catalogRepository.save(catalog);
        return catalog;
    }

    // ===================================================================================
    // UC-KON-01: Preparing and finalizing the vehicle specification — main scenario
    // ===================================================================================
    @Test
    void uc01_shouldBuildAndApproveVehicleSpecification() {
        // Precondition: a configurator session opened over a published price list
        ProductCatalog catalog = createPublishedCatalog();
        String catalogId = catalog.getCatalogId().value();

        // Steps 1-3: the Customer selects a package, engine, gearbox and color
        SpecificationId specId = buildSpecificationUseCase.startSpecification(catalogId);
        buildSpecificationUseCase.addOption(specId.value(), catalogId, "PAKIET_A1");
        buildSpecificationUseCase.addOption(specId.value(), catalogId, "SILNIK_B1");
        buildSpecificationUseCase.addOption(specId.value(), catalogId, "SKRZYNIA_C1");
        buildSpecificationUseCase.addOption(specId.value(), catalogId, "KOLOR_CZARNY");

        // Steps 5-7: the Customer finalizes, the system verifies completeness and saves
        buildSpecificationUseCase.finalizeSpecification(specId.value());

        // Postcondition: the finalized specification in the local configurator database
        VehicleSpecification saved = specificationRepository.findById(specId).orElseThrow();
        assertThat(saved.state()).isEqualTo(SpecificationState.FINAL);
        assertThat(saved.getSelectedOptions()).hasSize(4);
        assertThat(saved.getTotalPrice().amount()).isEqualByComparingTo("46000"); // 15000+20000+8000+3000

        // Postcondition: the system emits the SpecificationCompleted event onto the bus
        verify(rabbitTemplate).convertAndSend(
                anyString(), eq("specification.completed"), any(SpecificationCompletedEvent.class));
    }

    // ===================================================================================
    // UC-KON-01, A1: a disallowed combination (engine B2 with gearbox C1)
    // ===================================================================================
    @Test
    void uc01_a1_shouldBlockForbiddenCombinationAndEmitNoEvent() {
        ProductCatalog catalog = createPublishedCatalog();
        String catalogId = catalog.getCatalogId().value();

        SpecificationId specId = buildSpecificationUseCase.startSpecification(catalogId);
        buildSpecificationUseCase.addOption(specId.value(), catalogId, "SKRZYNIA_C1");

        // Step 4: the system detects on the fly a combination blocked by the manufacturer
        assertThatThrownBy(() ->
                buildSpecificationUseCase.addOption(specId.value(), catalogId, "SILNIK_B2"))
                .isInstanceOf(RuleViolationException.class);

        // The system asks to change the engine/gearbox/package — the faulty option did not enter the database
        VehicleSpecification saved = specificationRepository.findById(specId).orElseThrow();
        assertThat(saved.state()).isEqualTo(SpecificationState.IN_PROGRESS);
        assertThat(saved.getSelectedOptions()).containsExactly(new OptionCode("SKRZYNIA_C1"));

        // The context does not emit the final event
        verify(rabbitTemplate, never()).convertAndSend(
                anyString(), eq("specification.completed"), any(SpecificationCompletedEvent.class));
    }

    // ===================================================================================
    // UC-KON-01, step 6: the final completeness check blocks finalization
    // ===================================================================================
    @Test
    void uc01_shouldRejectApprovalOfIncompleteConfiguration() {
        ProductCatalog catalog = createPublishedCatalog();
        String catalogId = catalog.getCatalogId().value();

        // The Customer selected engine B2 with gearbox C2... but removed the gearbox — the selection is incomplete
        SpecificationId specId = buildSpecificationUseCase.startSpecification(catalogId);
        buildSpecificationUseCase.addOption(specId.value(), catalogId, "PAKIET_A1");
        buildSpecificationUseCase.addOption(specId.value(), catalogId, "SILNIK_B2");

        // Step 6: completeness (SILNIK_B2 requires SKRZYNIA_C2) blocks finalization
        assertThatThrownBy(() ->
                buildSpecificationUseCase.finalizeSpecification(specId.value()))
                .isInstanceOf(RuleViolationException.class);

        // The specification remains unfinalized, without the final event
        VehicleSpecification saved = specificationRepository.findById(specId).orElseThrow();
        assertThat(saved.state()).isEqualTo(SpecificationState.IN_PROGRESS);
        verify(rabbitTemplate, never()).convertAndSend(
                anyString(), eq("specification.completed"), any(SpecificationCompletedEvent.class));
    }

    // ===================================================================================
    // UC-KON-01, A2: Session interruption — the configuration saved as a draft version
    // ===================================================================================
    @Test
    void uc01_a2_shouldKeepAbandonedConfigurationAsDraft() {
        ProductCatalog catalog = createPublishedCatalog();

        // The Customer opens the configurator and leaves it before selecting options
        SpecificationId specId = buildSpecificationUseCase.startSpecification(catalog.getCatalogId().value());

        // The system saved the configuration as a draft version (DRAFT) in the local database
        VehicleSpecification draft = specificationRepository.findById(specId).orElseThrow();
        assertThat(draft.state()).isEqualTo(SpecificationState.DRAFT);
        assertThat(draft.getSelectedOptions()).isEmpty();
    }

    // ===================================================================================
    // UC-KON-02: Automatic update of the price list and catalog — main scenario
    // ===================================================================================
    @Test
    void uc02_shouldUpdateCatalogArchiveOldVersionAndEmitCatalogUpdated() {
        // Precondition: a previous price list version is active in the database
        ProductCatalog previous = ProductCatalog.createActive("MY_2025");
        catalogRepository.save(previous);

        // Steps 1-2: the Importer (Blackbox) provides a new data package; the ACL translates it into the local model
        when(importerApi.fetchCurrentOptions("MY_2026")).thenReturn(List.of(
                new CatalogOption(new OptionCode("PAKIET_A1"), Money.of(15500, "PLN")),
                new CatalogOption(new OptionCode("SILNIK_B1"), Money.of(21000, "PLN"))));

        // A signal from the webhook/bus triggers the update
        CatalogId newCatalogId = updateCatalogUseCase.publishNewCatalogVersion(
                "MY_2026", previous.getCatalogId().value());

        // Step 4: the new catalog saved in the local database, the previous one marked as archived
        ProductCatalog newCatalog = catalogRepository.findById(newCatalogId).orElseThrow();
        assertThat(newCatalog.state()).isEqualTo(CatalogState.ACTIVE);
        assertThat(newCatalog.getOptions()).hasSize(2);
        ProductCatalog archived = catalogRepository.findById(previous.getCatalogId()).orElseThrow();
        assertThat(archived.state()).isEqualTo(CatalogState.ARCHIVED);

        // Step 5: the context emits the CatalogUpdated event onto the data bus
        verify(rabbitTemplate).convertAndSend(
                anyString(), eq("catalog.updated"), any(CatalogUpdatedEvent.class));
    }

    // ===================================================================================
    // UC-KON-02, A1: a validation or data translation error — the package is rejected
    // ===================================================================================
    @Test
    void uc02_a1_shouldRejectInvalidPackageAndEmitCatalogUpdateFailed() {
        // In step 2 the translation layer (ACL) detects a critical error (e.g. missing prices, wrong format)
        when(importerApi.fetchCurrentOptions("MY_2026"))
                .thenThrow(new IllegalArgumentException("No prices in the catalog package"));

        int catalogsBefore = catalogRepository.findAll().size();

        // The system aborts the update and rejects the package
        assertThatThrownBy(() -> updateCatalogUseCase.publishNewCatalogVersion("MY_2026", null))
                .isInstanceOf(IllegalStateException.class);

        // ...emitting a technical integration-error event (CatalogUpdateFailed)
        verify(rabbitTemplate).convertAndSend(
                anyString(), eq("catalog.update_failed"), any(CatalogUpdateFailedEvent.class));
        verify(rabbitTemplate, never()).convertAndSend(
                anyString(), eq("catalog.updated"), any(CatalogUpdatedEvent.class));

        // The local database stays untouched — no catalog arrived
        assertThat(catalogRepository.findAll()).hasSize(catalogsBefore);
    }
}
