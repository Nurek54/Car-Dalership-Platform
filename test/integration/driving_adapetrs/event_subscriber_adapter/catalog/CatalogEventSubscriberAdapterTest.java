package integration.driving_adapetrs.event_subscriber_adapter.catalog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.sales.application.OfferAppService;
import salon.shared.events.CatalogVersionPublishedEvent;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogEventSubscriberAdapterTest {

    // Kaskader dla usługi aplikacji w Kontekście Sprzedaży
    @Mock
    private OfferAppService offerAppService;

    @InjectMocks
    private CatalogEventSubscriberAdapter adapter;

    // 1. HAPPY PATH
    @Test
    void shouldInvalidateOldOffersWhenNewCatalogIsPublished() {
        // Arrange
        CatalogVersionPublishedEvent validEvent = new CatalogVersionPublishedEvent(
                "EVT-5001",
                "CAT-2026", // Nowy identyfikator katalogu
                "2026"      // Rocznik modelowy
        );

        // Act
        adapter.handleCatalogVersionPublishedEvent(validEvent);

        // Assert - Sprawdzamy, czy adapter wywołał odpowiedni przypadek użycia (unieważnianie starych ofert)
        verify(offerAppService, times(1)).invalidateOffersForOlderCatalogs("CAT-2026");
    }

    // 2. WALIDACJA WEJŚCIA
    @Test
    void shouldRejectEventWhenCatalogIdIsMissing() {
        // Arrange
        CatalogVersionPublishedEvent invalidEvent = new CatalogVersionPublishedEvent(
                "EVT-5002",
                "", // Pusty identyfikator katalogu!
                "2026"
        );

        // Act & Assert
        assertThatThrownBy(() -> adapter.handleCatalogVersionPublishedEvent(invalidEvent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Identyfikator katalogu (catalogId) jest wymagany");

        verify(offerAppService, never()).invalidateOffersForOlderCatalogs(any());
    }

    // 3. MAPOWANIE BŁĘDÓW / DLQ
    @Test
    void shouldBubbleUpExceptionWhenAppServiceFailsToProcessCatalogUpdate() {
        // Arrange
        CatalogVersionPublishedEvent validEvent = new CatalogVersionPublishedEvent(
                "EVT-5003", "CAT-2026", "2026"
        );

        // Symulacja np. zablokowanego (Lock) rekordu w bazie danych
        doThrow(new IllegalStateException("Nie można uzyskać dostępu do tabeli Ofert"))
                .when(offerAppService).invalidateOffersForOlderCatalogs(any());

        // Act & Assert
        // Upewniamy się, że wyjątek nie został "zjedzony" przez pusty blok try-catch
        assertThatThrownBy(() -> adapter.handleCatalogVersionPublishedEvent(validEvent))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Nie można uzyskać dostępu do tabeli Ofert");
    }
}