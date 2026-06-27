package acceptance.catalog_and_configurator_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import salon.catalog.application.command.AddOptionCommand;
import salon.catalog.application.command.FinalizeSpecificationCommand;
import salon.catalog.application.command.InitiateConfiguratorSessionCommand;
import salon.catalog.application.domain.exception.CombinationNotAllowedException;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.specification.SpecificationId;
import salon.catalog.application.domain.model.specification.SpecificationState;
import salon.catalog.application.domain.model.specification.VehicleSpecification;
import salon.catalog.application.dto.SpecificationView;
import salon.catalog.application.port.in.BuildSpecification;
import salon.catalog.application.port.in.UpdateCatalog;
import salon.catalog.application.port.out.VehicleSpecificationRepository;
import salon.catalog.application.port.out.ProductCatalogRepository;
import salon.catalog.infrastructure.out.acl.ExternalCatalogPackage;
import salon.catalog.infrastructure.out.acl.ImporterServiceClient;
import salon.catalog.infrastructure.out.messaging.MessageBroker;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testy akceptacyjne kontekstu Katalogu i Konfiguratora — pełne przypadki użycia end-to-end
 * (przez porty wejściowe) z prawdziwą persystencją w pamięci i zamockowanymi granicami zewnętrznymi.
 */
@SpringBootTest
class CatalogContextAcceptanceTest {

    @Autowired private UpdateCatalog updateCatalog;
    @Autowired private BuildSpecification buildSpecification;
    @Autowired private ProductCatalogRepository catalogRepository;
    @Autowired private VehicleSpecificationRepository specificationRepository;

    @MockBean private ImporterServiceClient importerServiceClient;  // granica systemu producenta
    @MockBean private MessageBroker messageBroker;                  // granica szyny zdarzeń

    /** Buduje zewnętrzny pakiet dostawcy (B2, C1, A1) z regułą B2 INCOMPATIBLE_WITH C1. */
    private ExternalCatalogPackage validPackage(int year) {
        return new ExternalCatalogPackage(
                year,
                List.of(
                        new ExternalCatalogPackage.ExternalItem("B2", 1_000_000, "PLN"),
                        new ExternalCatalogPackage.ExternalItem("C1", 500_000, "PLN"),
                        new ExternalCatalogPackage.ExternalItem("A1", 200_000, "PLN")),
                List.of(new ExternalCatalogPackage.ExternalRestriction("B2", "C1", "INCOMPATIBLE_WITH")));
    }

    // ===================================================================================
    // UC-KON-02: Automatyczna aktualizacja katalogu i cennika
    // ===================================================================================
    @Test
    void uc02_shouldUpdateCatalogAndPublishEvent() {
        // System producenta udostępnia poprawny pakiet cennika dla rocznika 2025
        when(importerServiceClient.downloadLatestPackage()).thenReturn(validPackage(2025));

        // CronJob/port wejściowy inicjuje aktualizację
        updateCatalog.update();

        // W bazie pojawia się aktywny cennik, a na szynę trafia zdarzenie CatalogUpdated
        assertThat(catalogRepository.findActiveByModelYear(ModelYear.of(2025))).isPresent();
        verify(messageBroker).send(eq("catalog.CatalogUpdated"), anyString());
    }

    @Test
    void uc02_shouldEmitFailedEventOnInvalidPackage() {
        // Pakiet z nieznanym typem ograniczenia = błąd translacji
        when(importerServiceClient.downloadLatestPackage()).thenReturn(new ExternalCatalogPackage(
                2030,
                List.of(new ExternalCatalogPackage.ExternalItem("B2", 1_000_000, "PLN")),
                List.of(new ExternalCatalogPackage.ExternalRestriction("B2", "C1", "NIEZNANY_TYP"))));

        updateCatalog.update();

        // Pakiet odrzucony — emisja CatalogUpdateFailed i brak aktywnego cennika
        verify(messageBroker).send(eq("catalog.CatalogUpdateFailed"), anyString());
        assertThat(catalogRepository.findActiveByModelYear(ModelYear.of(2030))).isEmpty();
    }

    // ===================================================================================
    // UC-KON-01: Opracowanie i zatwierdzenie specyfikacji pojazdu
    // ===================================================================================
    @Test
    void uc01_shouldConfigureAndFinalizeSpecification() {
        // Najpierw zasilamy aktywny cennik dla rocznika 2026
        when(importerServiceClient.downloadLatestPackage()).thenReturn(validPackage(2026));
        updateCatalog.update();

        // Sprzedawca otwiera sesję konfiguratora — powstaje robocza specyfikacja
        SpecificationView draft = buildSpecification.initiate(new InitiateConfiguratorSessionCommand(2026));
        assertThat(draft.state()).isEqualTo(SpecificationState.DRAFT.name());
        String specId = draft.specificationId();

        // Klient dobiera niekolidujące opcje (B2 + A1)
        buildSpecification.addOption(new AddOptionCommand(specId, "B2"));
        SpecificationView withOptions = buildSpecification.addOption(new AddOptionCommand(specId, "A1"));
        assertThat(withOptions.state()).isEqualTo(SpecificationState.IN_PROGRESS.name());

        // Finalizacja przenosi specyfikację do FINAL i emituje SpecificationCompleted
        SpecificationView finalized =
                buildSpecification.finalizeSpecification(new FinalizeSpecificationCommand(specId));
        assertThat(finalized.state()).isEqualTo(SpecificationState.FINAL.name());
        verify(messageBroker).send(eq("catalog.SpecificationCompleted"), anyString());
    }

    @Test
    void uc01_shouldBlockExcludedCombination() {
        // Aktywny cennik dla rocznika 2027 z regułą wykluczenia B2/C1
        when(importerServiceClient.downloadLatestPackage()).thenReturn(validPackage(2027));
        updateCatalog.update();

        SpecificationView draft = buildSpecification.initiate(new InitiateConfiguratorSessionCommand(2027));
        String specId = draft.specificationId();

        // Klient dodaje silnik B2, a następnie kolidującą skrzynię C1
        buildSpecification.addOption(new AddOptionCommand(specId, "B2"));

        // Reguła wykluczenia blokuje niedozwoloną kombinację (scenariusz A1)
        assertThatThrownBy(() -> buildSpecification.addOption(new AddOptionCommand(specId, "C1")))
                .isInstanceOf(CombinationNotAllowedException.class);
    }
    @Test
    void uc01_a2_shouldPersistDraftWhenSessionInterrupted() {
        // Aktywny cennik dla rocznika 2028
        when(importerServiceClient.downloadLatestPackage()).thenReturn(validPackage(2028));
        updateCatalog.update();

        // Klient otwiera sesję i dobiera opcję, ale opuszcza konfigurator przed zatwierdzeniem
        SpecificationView draft = buildSpecification.initiate(new InitiateConfiguratorSessionCommand(2028));
        String specId = draft.specificationId();
        buildSpecification.addOption(new AddOptionCommand(specId, "B2"));

        // System zapisuje konfigurację jako wersję roboczą (IN_PROGRESS), bez zdarzenia końcowego
        VehicleSpecification saved = specificationRepository.findById(SpecificationId.of(specId)).orElseThrow();
        assertThat(saved.state()).isEqualTo(SpecificationState.IN_PROGRESS);
        verify(messageBroker, never()).send(eq("catalog.SpecificationCompleted"), anyString());
    }
}
