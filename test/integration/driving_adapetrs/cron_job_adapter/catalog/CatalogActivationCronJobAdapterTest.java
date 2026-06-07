package integration.driving_adapetrs.cron_job_adapter.catalog;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.catalog.application.CatalogAppService;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogActivationCronJobAdapterTest {

    // Kaskader dla usługi aplikacji w Kontekście Katalogu
    @Mock
    private CatalogAppService catalogAppService;

    @InjectMocks
    private CatalogActivationCronJobAdapter adapter;

    // 1. HAPPY PATH: Uruchomienie aktywacji zaplanowanych cenników
    @Test
    void shouldTriggerActivationOfScheduledCatalogs() {
        // Act
        adapter.activateScheduledCatalogsJob();

        // Assert
        // Weryfikacja delegacji do usługi, która znajdzie cenniki z datą "na dzisiaj" i je aktywuje
        verify(catalogAppService, times(1)).activatePendingCatalogs();
    }

    // 2. NIEZAWODNOŚĆ: Ochrona harmonogramu przed awarią
    @Test
    void shouldNotBubbleUpExceptionWhenCatalogActivationFails() {
        // Arrange
        // Symulacja błędu - np. brak dostępu do bazy danych lub zablokowany rekord
        doThrow(new IllegalStateException("Zakleszczenie w bazie danych podczas aktywacji katalogu"))
                .when(catalogAppService).activatePendingCatalogs();

        // Act & Assert
        // Upewniamy się, że błąd jest "połykany" przez adapter i logowany, a nie wyrzucany do Springa
        assertThatCode(() -> adapter.activateScheduledCatalogsJob())
                .doesNotThrowAnyException();

        verify(catalogAppService, times(1)).activatePendingCatalogs();
    }
}