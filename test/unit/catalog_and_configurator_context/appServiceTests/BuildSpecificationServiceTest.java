package unit.catalog_and_configurator_context.appServiceTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.catalog.application.command.AddOptionCommand;
import salon.catalog.application.command.FinalizeSpecificationCommand;
import salon.catalog.application.command.InitiateConfiguratorSessionCommand;
import salon.catalog.application.domain.exception.CatalogNotFoundException;
import salon.catalog.application.domain.exception.CombinationNotAllowedException;
import salon.catalog.application.domain.exception.SpecificationNotFoundException;
import salon.catalog.application.domain.model.catalog.CatalogOption;
import salon.catalog.application.domain.model.catalog.CatalogRule;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.catalog.ProductCatalog;
import salon.catalog.application.domain.model.catalog.ProductCatalogFactory;
import salon.catalog.application.domain.model.catalog.RuleType;
import salon.catalog.application.domain.model.event.SpecificationCompleted;
import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;
import salon.catalog.application.domain.model.specification.SpecificationState;
import salon.catalog.application.domain.model.specification.VehicleSpecification;
import salon.catalog.application.domain.model.specification.VehicleSpecificationFactory;
import salon.catalog.application.domain.service.RuleValidationService;
import salon.catalog.application.dto.SpecificationView;
import salon.catalog.application.port.out.EventPublisher;
import salon.catalog.application.port.out.ProductCatalogRepository;
import salon.catalog.application.port.out.VehicleSpecificationRepository;
import salon.catalog.application.service.BuildSpecificationService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** UC-KON-01: Konfiguracja pojazdu — serwis aplikacyjny BuildSpecificationService. */
@ExtendWith(MockitoExtension.class)
class BuildSpecificationServiceTest {

    @Mock private ProductCatalogRepository catalogRepository;
    @Mock private VehicleSpecificationRepository specificationRepository;
    @Mock private EventPublisher eventPublisher;

    private BuildSpecificationService service;

    private final ProductCatalogFactory catalogFactory = new ProductCatalogFactory();
    private final VehicleSpecificationFactory specFactory = new VehicleSpecificationFactory();
    private final Clock clock = Clock.fixed(Instant.parse("2025-06-01T10:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        service = new BuildSpecificationService(catalogRepository, specificationRepository,
                new RuleValidationService(), specFactory, eventPublisher, clock);
    }

    private CatalogOption option(String code, String price) {
        return new CatalogOption(OptionCode.of(code), Money.of(new BigDecimal(price), "PLN"));
    }

    @Test
    void initiateShouldOpenSessionForActiveCatalog() {
        // Istnieje aktywny cennik dla rocznika 2025
        ProductCatalog catalog = catalogFactory.createNew(
                ModelYear.of(2025), List.of(option("B2", "10000")), List.of());
        when(catalogRepository.findActiveByModelYear(ModelYear.of(2025))).thenReturn(Optional.of(catalog));

        // Otwarcie sesji konfiguratora tworzy roboczą specyfikację
        SpecificationView view = service.initiate(new InitiateConfiguratorSessionCommand(2025));

        verify(specificationRepository).save(any(VehicleSpecification.class));
        assertThat(view.state()).isEqualTo(SpecificationState.DRAFT.name());
    }

    @Test
    void initiateShouldFailWhenNoActiveCatalog() {
        // Brak aktywnego cennika dla podanego rocznika
        when(catalogRepository.findActiveByModelYear(ModelYear.of(1999))).thenReturn(Optional.empty());

        // Serwis przerywa proces i zgłasza błąd biznesowy
        assertThatThrownBy(() -> service.initiate(new InitiateConfiguratorSessionCommand(1999)))
                .isInstanceOf(CatalogNotFoundException.class);
        verify(specificationRepository, never()).save(any());
    }

    @Test
    void addOptionShouldRecalculatePriceAndSave() {
        ProductCatalog catalog = catalogFactory.createNew(
                ModelYear.of(2025), List.of(option("B2", "10000")), List.of());
        VehicleSpecification spec = specFactory.createDraft(catalog.id(), "PLN");
        when(specificationRepository.findById(spec.id())).thenReturn(Optional.of(spec));
        when(catalogRepository.findById(catalog.id())).thenReturn(Optional.of(catalog));

        // Dodanie opcji przelicza cenę i zapisuje specyfikację
        SpecificationView view = service.addOption(new AddOptionCommand(spec.id().toString(), "B2"));

        verify(specificationRepository).save(spec);
        assertThat(view.state()).isEqualTo(SpecificationState.IN_PROGRESS.name());
        assertThat(view.totalPrice()).isEqualByComparingTo(new BigDecimal("10000"));
    }

    @Test
    void addOptionShouldBlockExcludedCombinationAndNotSave() {
        // Cennik z regułą wykluczenia B2 EXCLUDES C1
        ProductCatalog catalog = catalogFactory.createNew(
                ModelYear.of(2025),
                List.of(option("B2", "10000"), option("C1", "5000")),
                List.of(new CatalogRule(OptionCode.of("B2"), OptionCode.of("C1"), RuleType.EXCLUDES)));
        VehicleSpecification spec = specFactory.createDraft(catalog.id(), "PLN");
        spec.addOption(OptionCode.of("B2"), catalog); // klient ma już silnik B2
        when(specificationRepository.findById(spec.id())).thenReturn(Optional.of(spec));
        when(catalogRepository.findById(catalog.id())).thenReturn(Optional.of(catalog));

        // Próba dodania kolidującej opcji C1 jest blokowana, transakcja jest przerywana
        assertThatThrownBy(() -> service.addOption(new AddOptionCommand(spec.id().toString(), "C1")))
                .isInstanceOf(CombinationNotAllowedException.class);
        verify(specificationRepository, never()).save(any());
    }

    @Test
    void finalizeShouldEmitSpecificationCompleted() {
        ProductCatalog catalog = catalogFactory.createNew(
                ModelYear.of(2025), List.of(option("B2", "10000")), List.of());
        VehicleSpecification spec = specFactory.createDraft(catalog.id(), "PLN");
        spec.addOption(OptionCode.of("B2"), catalog);
        when(specificationRepository.findById(spec.id())).thenReturn(Optional.of(spec));
        when(catalogRepository.findById(catalog.id())).thenReturn(Optional.of(catalog));

        // Finalizacja zapisuje specyfikację i emituje zdarzenie SpecificationCompleted
        SpecificationView view = service.finalizeSpecification(
                new FinalizeSpecificationCommand(spec.id().toString()));

        verify(specificationRepository).save(spec);
        verify(eventPublisher).publish(any(SpecificationCompleted.class));
        assertThat(view.state()).isEqualTo(SpecificationState.FINAL.name());
    }

    @Test
    void shouldThrowWhenSpecificationNotFound() {
        // Nieznany identyfikator specyfikacji
        VehicleSpecification spec = specFactory.createDraft(
                salon.catalog.application.domain.model.catalog.CatalogId.generate(), "PLN");
        when(specificationRepository.findById(spec.id())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.addOption(new AddOptionCommand(spec.id().toString(), "B2")))
                .isInstanceOf(SpecificationNotFoundException.class);
    }
    @Test
    void shouldSaveWorkingDraftWithoutEmittingEventWhenSessionInterrupted() { // A2 Przerwanie sesji
        ProductCatalog catalog = catalogFactory.createNew(
                ModelYear.of(2025), List.of(option("B2", "10000")), List.of());
        VehicleSpecification spec = specFactory.createDraft(catalog.id(), "PLN");
        when(specificationRepository.findById(spec.id())).thenReturn(Optional.of(spec));
        when(catalogRepository.findById(catalog.id())).thenReturn(Optional.of(catalog));

        // Klient dobiera opcję, ale opuszcza konfigurator bez zatwierdzenia
        service.addOption(new AddOptionCommand(spec.id().toString(), "B2"));

        // Konfiguracja jest zapisana jako wersja robocza (IN_PROGRESS), bez zdarzenia SpecificationCompleted
        verify(specificationRepository).save(spec);
        verify(eventPublisher, never()).publish(any());
        assertThat(spec.state()).isEqualTo(SpecificationState.IN_PROGRESS);
    }
}
