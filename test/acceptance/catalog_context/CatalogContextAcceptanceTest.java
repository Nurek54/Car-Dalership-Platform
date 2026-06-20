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
 * Testy akceptacyjne Kontekstu Katalogu i Konfiguratora (UC-KON-01, UC-KON-02).
 *
 * Kontekst nie wystawia adapterów REST — zgodnie z diagramami sekwencji sterują nim:
 * konfigurator Klienta (BuildSpecificationUseCase) oraz webhook/cron Importera
 * (UpdateCatalogUseCase). Testy wchodzą więc przez porty wejściowe (use case'y),
 * a weryfikują warunki końcowe tam, gdzie w sales: w fizycznej bazie SQL i na szynie
 * zdarzeń (RabbitTemplate). Importer (Blackbox) jest zaślepiony na poziomie portu ACL.
 */
@SpringBootTest
class CatalogContextAcceptanceTest {

    /** Korzeń kompozycji na potrzeby testu: usługi Katalogu są czystymi POJO (bez @Service). */
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

    @MockBean private ImporterApiPort importerApi;   // Zewnętrzny system producenta/importera (Blackbox)
    @MockBean private RabbitTemplate rabbitTemplate; // Szyna zdarzeń

    // Cennik wg dokumentacji UC-KON-01: pakiety (A1, A2), silniki (B1, B2), skrzynie (C1, C2), kolor.
    // Reguła producenta: silnik B2 nie może być wybrany ze skrzynią C1 (EXCLUDES),
    // a silnik B2 wymaga skrzyni C2 (REQUIRES — weryfikacja kompletności w kroku 6).
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
    // UC-KON-01: Opracowanie i zatwierdzenie specyfikacji pojazdu — scenariusz główny
    // ===================================================================================
    @Test
    void uc01_shouldBuildAndApproveVehicleSpecification() {
        // Warunek wstępny: sesja konfiguratora otwarta nad opublikowanym cennikiem
        ProductCatalog catalog = createPublishedCatalog();
        String catalogId = catalog.getCatalogId().value();

        // Kroki 1-3: Klient wybiera pakiet, silnik, skrzynię i kolor
        SpecificationId specId = buildSpecificationUseCase.startSpecification(catalogId);
        buildSpecificationUseCase.addOption(specId.value(), catalogId, "PAKIET_A1");
        buildSpecificationUseCase.addOption(specId.value(), catalogId, "SILNIK_B1");
        buildSpecificationUseCase.addOption(specId.value(), catalogId, "SKRZYNIA_C1");
        buildSpecificationUseCase.addOption(specId.value(), catalogId, "KOLOR_CZARNY");

        // Kroki 5-7: Klient zatwierdza, system weryfikuje kompletność i zapisuje
        buildSpecificationUseCase.finalizeSpecification(specId.value());

        // Warunek końcowy: zatwierdzona specyfikacja w lokalnej bazie konfiguratora
        VehicleSpecification saved = specificationRepository.findById(specId).orElseThrow();
        assertThat(saved.state()).isEqualTo(SpecificationState.FINAL);
        assertThat(saved.getSelectedOptions()).hasSize(4);
        assertThat(saved.getTotalPrice().amount()).isEqualByComparingTo("46000"); // 15000+20000+8000+3000

        // Warunek końcowy: system emituje zdarzenie SpecificationCompleted na szynę
        verify(rabbitTemplate).convertAndSend(
                anyString(), eq("specification.completed"), any(SpecificationCompletedEvent.class));
    }

    // ===================================================================================
    // UC-KON-01, A1: Niedozwolona kombinacja (silnik B2 ze skrzynią C1)
    // ===================================================================================
    @Test
    void uc01_a1_shouldBlockForbiddenCombinationAndEmitNoEvent() {
        ProductCatalog catalog = createPublishedCatalog();
        String catalogId = catalog.getCatalogId().value();

        SpecificationId specId = buildSpecificationUseCase.startSpecification(catalogId);
        buildSpecificationUseCase.addOption(specId.value(), catalogId, "SKRZYNIA_C1");

        // Krok 4: system na bieżąco wykrywa kombinację zablokowaną przez producenta
        assertThatThrownBy(() ->
                buildSpecificationUseCase.addOption(specId.value(), catalogId, "SILNIK_B2"))
                .isInstanceOf(RuleViolationException.class);

        // System prosi o zmianę silnika/skrzyni/pakietu — wadliwa opcja nie weszła do bazy
        VehicleSpecification saved = specificationRepository.findById(specId).orElseThrow();
        assertThat(saved.state()).isEqualTo(SpecificationState.IN_PROGRESS);
        assertThat(saved.getSelectedOptions()).containsExactly(new OptionCode("SKRZYNIA_C1"));

        // Kontekst nie emituje zdarzenia końcowego
        verify(rabbitTemplate, never()).convertAndSend(
                anyString(), eq("specification.completed"), any(SpecificationCompletedEvent.class));
    }

    // ===================================================================================
    // UC-KON-01, krok 6: weryfikacja ostatecznej kompletności blokuje zatwierdzenie
    // ===================================================================================
    @Test
    void uc01_shouldRejectApprovalOfIncompleteConfiguration() {
        ProductCatalog catalog = createPublishedCatalog();
        String catalogId = catalog.getCatalogId().value();

        // Klient wybrał silnik B2 ze skrzynią C2... ale usunął skrzynię — wybór niekompletny
        SpecificationId specId = buildSpecificationUseCase.startSpecification(catalogId);
        buildSpecificationUseCase.addOption(specId.value(), catalogId, "PAKIET_A1");
        buildSpecificationUseCase.addOption(specId.value(), catalogId, "SILNIK_B2");

        // Krok 6: kompletność (SILNIK_B2 wymaga SKRZYNIA_C2) blokuje zatwierdzenie
        assertThatThrownBy(() ->
                buildSpecificationUseCase.finalizeSpecification(specId.value()))
                .isInstanceOf(RuleViolationException.class);

        // Specyfikacja pozostaje niezatwierdzona, bez zdarzenia końcowego
        VehicleSpecification saved = specificationRepository.findById(specId).orElseThrow();
        assertThat(saved.state()).isEqualTo(SpecificationState.IN_PROGRESS);
        verify(rabbitTemplate, never()).convertAndSend(
                anyString(), eq("specification.completed"), any(SpecificationCompletedEvent.class));
    }

    // ===================================================================================
    // UC-KON-01, A2: Przerwanie sesji — konfiguracja zapisana jako wersja robocza
    // ===================================================================================
    @Test
    void uc01_a2_shouldKeepAbandonedConfigurationAsDraft() {
        ProductCatalog catalog = createPublishedCatalog();

        // Klient otwiera konfigurator i opuszcza go przed wyborem opcji
        SpecificationId specId = buildSpecificationUseCase.startSpecification(catalog.getCatalogId().value());

        // System zapisał konfigurację jako wersję roboczą (DRAFT) w lokalnej bazie
        VehicleSpecification draft = specificationRepository.findById(specId).orElseThrow();
        assertThat(draft.state()).isEqualTo(SpecificationState.DRAFT);
        assertThat(draft.getSelectedOptions()).isEmpty();
    }

    // ===================================================================================
    // UC-KON-02: Automatyczna aktualizacja cennika i katalogu — scenariusz główny
    // ===================================================================================
    @Test
    void uc02_shouldUpdateCatalogArchiveOldVersionAndEmitCatalogUpdated() {
        // Warunek wstępny: w bazie aktywna poprzednia wersja cennika
        ProductCatalog previous = ProductCatalog.createActive("MY_2025");
        catalogRepository.save(previous);

        // Krok 1-2: Importer (Blackbox) udostępnia nowy pakiet danych; ACL tłumaczy go na model lokalny
        when(importerApi.fetchCurrentOptions("MY_2026")).thenReturn(List.of(
                new CatalogOption(new OptionCode("PAKIET_A1"), Money.of(15500, "PLN")),
                new CatalogOption(new OptionCode("SILNIK_B1"), Money.of(21000, "PLN"))));

        // Sygnał z webhooka/szyny uruchamia aktualizację
        CatalogId newCatalogId = updateCatalogUseCase.publishNewCatalogVersion(
                "MY_2026", previous.getCatalogId().value());

        // Krok 4: nowy katalog zapisany w lokalnej bazie, poprzedni oznaczony jako archiwalny
        ProductCatalog newCatalog = catalogRepository.findById(newCatalogId).orElseThrow();
        assertThat(newCatalog.state()).isEqualTo(CatalogState.ACTIVE);
        assertThat(newCatalog.getOptions()).hasSize(2);
        ProductCatalog archived = catalogRepository.findById(previous.getCatalogId()).orElseThrow();
        assertThat(archived.state()).isEqualTo(CatalogState.ARCHIVED);

        // Krok 5: kontekst emituje na szynę danych zdarzenie CatalogUpdated
        verify(rabbitTemplate).convertAndSend(
                anyString(), eq("catalog.updated"), any(CatalogUpdatedEvent.class));
    }

    // ===================================================================================
    // UC-KON-02, A1: Błąd walidacji lub translacji danych — pakiet odrzucony
    // ===================================================================================
    @Test
    void uc02_a1_shouldRejectInvalidPackageAndEmitCatalogUpdateFailed() {
        // W kroku 2 warstwa translacji (ACL) wykrywa błąd krytyczny (np. brak cen, zły format)
        when(importerApi.fetchCurrentOptions("MY_2026"))
                .thenThrow(new IllegalArgumentException("Brak cen w pakiecie katalogowym"));

        int catalogsBefore = catalogRepository.findAll().size();

        // System przerywa aktualizację i odrzuca pakiet
        assertThatThrownBy(() -> updateCatalogUseCase.publishNewCatalogVersion("MY_2026", null))
                .isInstanceOf(IllegalStateException.class);

        // ...emitując techniczne zdarzenie o błędzie integracji (CatalogUpdateFailed)
        verify(rabbitTemplate).convertAndSend(
                anyString(), eq("catalog.update_failed"), any(CatalogUpdateFailedEvent.class));
        verify(rabbitTemplate, never()).convertAndSend(
                anyString(), eq("catalog.updated"), any(CatalogUpdatedEvent.class));

        // Lokalna baza pozostaje nietknięta — żaden katalog nie przybył
        assertThat(catalogRepository.findAll()).hasSize(catalogsBefore);
    }
}
