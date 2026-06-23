package unit.catalog_and_configurator_context.appServiceTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.catalog.application.domain.model.catalog.CatalogOption;
import salon.catalog.application.domain.model.catalog.CatalogRule;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.catalog.ProductCatalog;
import salon.catalog.application.domain.model.catalog.ProductCatalogFactory;
import salon.catalog.application.domain.model.catalog.RuleType;
import salon.catalog.application.domain.model.event.CatalogUpdateFailed;
import salon.catalog.application.domain.model.event.CatalogUpdated;
import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;
import salon.catalog.application.domain.service.RuleValidationService;
import salon.catalog.application.dto.ImportedCatalogData;
import salon.catalog.application.port.out.CatalogImporterPort;
import salon.catalog.application.port.out.EventPublisher;
import salon.catalog.application.port.out.ProductCatalogRepository;
import salon.catalog.application.service.UpdateCatalogService;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** UC-KON-02: Automatyczna aktualizacja cennika — serwis aplikacyjny UpdateCatalogService. */
@ExtendWith(MockitoExtension.class)
class UpdateCatalogServiceTest {

    @Mock private CatalogImporterPort catalogImporter;
    @Mock private ProductCatalogRepository catalogRepository;
    @Mock private EventPublisher eventPublisher;

    private UpdateCatalogService service;

    private final ProductCatalogFactory catalogFactory = new ProductCatalogFactory();
    private final Clock clock = Clock.fixed(Instant.parse("2025-06-01T10:00:00Z"), ZoneOffset.UTC);

    @BeforeEach
    void setUp() {
        service = new UpdateCatalogService(catalogImporter, catalogRepository,
                new RuleValidationService(), catalogFactory, eventPublisher, clock);
    }

    private CatalogOption option(String code, String price) {
        return new CatalogOption(OptionCode.of(code), Money.of(new BigDecimal(price), "PLN"));
    }

    @Test
    void shouldSaveFirstVersionAndEmitCatalogUpdated() { // SCENARIUSZ GŁÓWNY
        // Importer dostarcza poprawny pakiet, brak wcześniejszego aktywnego cennika
        when(catalogImporter.fetchLatestCatalog()).thenReturn(
                new ImportedCatalogData(ModelYear.of(2025), List.of(option("B2", "10000")), List.of()));
        when(catalogRepository.findActiveByModelYear(ModelYear.of(2025))).thenReturn(Optional.empty());

        service.update();

        // Nowy cennik jest zapisany, a na szynę trafia zdarzenie CatalogUpdated
        verify(catalogRepository).save(any(ProductCatalog.class));
        verify(eventPublisher).publish(any(CatalogUpdated.class));
        verify(eventPublisher, never()).publish(any(CatalogUpdateFailed.class));
    }

    @Test
    void shouldArchivePreviousAndSaveNextVersion() {
        // Istnieje bieżący, aktywny cennik w wersji 1
        ProductCatalog current = catalogFactory.createNew(
                ModelYear.of(2025), List.of(option("B2", "10000")), List.of());
        when(catalogImporter.fetchLatestCatalog()).thenReturn(
                new ImportedCatalogData(ModelYear.of(2025), List.of(option("B2", "11000")), List.of()));
        when(catalogRepository.findActiveByModelYear(ModelYear.of(2025))).thenReturn(Optional.of(current));

        service.update();

        // Poprzedni cennik archiwizowany, nowy zapisany — dwa zapisy, jedno zdarzenie CatalogUpdated
        verify(catalogRepository, times(2)).save(any(ProductCatalog.class));
        verify(eventPublisher).publish(any(CatalogUpdated.class));
    }

    @Test
    void shouldEmitCatalogUpdateFailedOnConflictingRules() { // Scenariusz alternatywny A1
        // Pakiet ze sprzecznymi regułami: B2 jednocześnie EXCLUDES i REQUIRES C1
        when(catalogImporter.fetchLatestCatalog()).thenReturn(new ImportedCatalogData(
                ModelYear.of(2025),
                List.of(option("B2", "10000"), option("C1", "5000")),
                List.of(
                        new CatalogRule(OptionCode.of("B2"), OptionCode.of("C1"), RuleType.EXCLUDES),
                        new CatalogRule(OptionCode.of("B2"), OptionCode.of("C1"), RuleType.REQUIRES))));
        when(catalogRepository.findActiveByModelYear(ModelYear.of(2025))).thenReturn(Optional.empty());

        service.update();

        // Pakiet odrzucony — emisja CatalogUpdateFailed, brak zapisu i CatalogUpdated
        verify(eventPublisher).publish(any(CatalogUpdateFailed.class));
        verify(eventPublisher, never()).publish(any(CatalogUpdated.class));
        verify(catalogRepository, never()).save(any());
    }
}
