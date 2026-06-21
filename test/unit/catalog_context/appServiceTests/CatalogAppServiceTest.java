package unit.catalog_context.appServiceTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.catalog.application.port.out.CatalogRepository;
import salon.catalog.application.port.out.ImporterApiPort;
import salon.catalog.application.service.CatalogAppService;
import salon.catalog.domain.event.CatalogUpdateFailedEvent;
import salon.catalog.domain.event.CatalogUpdatedEvent;
import salon.catalog.domain.model.catalog.*;
import salon.shared.application.EventPublisherPort;
import salon.shared.model.Money;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** UC-KON-02: Automatic update of the price list and catalog */
@ExtendWith(MockitoExtension.class)
class CatalogAppServiceTest {

    @Mock private CatalogRepository catalogRepository;
    @Mock private ImporterApiPort importerApi;
    @Mock private EventPublisherPort eventPublisher;
    @InjectMocks private CatalogAppService catalogAppService;

    private List<CatalogOption> importerPackage() {
        return List.of(
                new CatalogOption(new OptionCode("LED_LIGHTS"), Money.of(4500, "PLN")),
                new CatalogOption(new OptionCode("PANORAMIC_ROOF"), Money.of(8000, "PLN")));
    }

    @Test
    void shouldPublishNewCatalogVersionAndEmitCatalogUpdatedEvent() {
        // The Importer responds with a valid package of options with prices
        when(importerApi.fetchCurrentOptions("MY_2026")).thenReturn(importerPackage());

        CatalogId newCatalogId = catalogAppService.publishNewCatalogVersion("MY_2026", null);

        // A new, active price list with options from the Importer goes to the repository
        ArgumentCaptor<ProductCatalog> captor = ArgumentCaptor.forClass(ProductCatalog.class);
        verify(catalogRepository).save(captor.capture());
        ProductCatalog saved = captor.getValue();
        assertThat(saved.getCatalogId()).isEqualTo(newCatalogId);
        assertThat(saved.state()).isEqualTo(CatalogState.ACTIVE);
        assertThat(saved.getOptions()).hasSize(2);

        // Change propagation: the CatalogUpdated event goes out into the world
        verify(eventPublisher).publish(any(CatalogUpdatedEvent.class));
    }

    @Test
    void shouldArchivePreviousCatalogWhenPublishingNewVersion() {
        // A previous, active price list version exists in the system
        ProductCatalog previous = ProductCatalog.createActive("MY_2025");
        when(catalogRepository.findById(previous.getCatalogId())).thenReturn(Optional.of(previous));
        when(importerApi.fetchCurrentOptions("MY_2026")).thenReturn(importerPackage());

        catalogAppService.publishNewCatalogVersion("MY_2026", previous.getCatalogId().value());

        // The old version is archived (immutability of old versions) and saved
        assertThat(previous.state()).isEqualTo(CatalogState.ARCHIVED);
        verify(catalogRepository, times(2)).save(any(ProductCatalog.class)); // stary + nowy
    }

    @Test
    void shouldAbortUpdateAndEmitFailureEventWhenImporterPackageIsInvalid() {
        // Scenario A1: a package translation/validation error on the ACL side
        when(importerApi.fetchCurrentOptions("MY_2026"))
                .thenThrow(new IllegalArgumentException("Niepoprawny format pakietu Importera"));

        // Aktualizacja zostaje przerwana
        assertThatThrownBy(() -> catalogAppService.publishNewCatalogVersion("MY_2026", null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Catalog update failed");

        // The integration-error signal (CatalogUpdateFailed) goes out into the world
        verify(eventPublisher).publish(any(CatalogUpdateFailedEvent.class));

        // No faulty price list is saved
        verify(catalogRepository, never()).save(any());
    }

    @Test
    void shouldRejectEmptyImporterPackage() {
        // The Importer returns an empty package (no options/prices) — logical validation rejects it
        when(importerApi.fetchCurrentOptions("MY_2026")).thenReturn(List.of());

        assertThatThrownBy(() -> catalogAppService.publishNewCatalogVersion("MY_2026", null))
                .isInstanceOf(IllegalStateException.class);

        verify(eventPublisher).publish(any(CatalogUpdateFailedEvent.class));
        verify(catalogRepository, never()).save(any());
    }
}
