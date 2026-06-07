package integration.driving_adapetrs.cron_job_adapter.sales;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.sales.application.OfferAppService;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

// Używamy czystego Mockito, ponieważ nie musimy podnosić całego Springa,
// by przetestować, czy adapter poprawnie woła usługę.
@ExtendWith(MockitoExtension.class)
class ExpiredOffersCronJobAdapterTest {

    @Mock
    private OfferAppService offerAppService;

    @InjectMocks
    private ExpiredOffersCronJobAdapter cronJobAdapter;

    // 1. HAPPY PATH: Czy harmonogram prawidłowo wywołuje logikę?
    @Test
    void shouldTriggerExpiredOffersInvalidation() {
        // Act
        // Wywołujemy metodę, która w kodzie produkcyjnym ma nad sobą np. @Scheduled(cron = "0 0 1 * * ?")
        cronJobAdapter.invalidateExpiredOffersJob();

        // Assert
        // Upewniamy się, że adapter obudził warstwę aplikacji i kazał jej posprzątać stare oferty
        verify(offerAppService, times(1)).processExpiredOffers();
    }

    // 2. NIEZAWODNOŚĆ (Brak wycieków wyjątków): Czy błąd nie zabije wątku schedulera?
    @Test
    void shouldNotThrowExceptionWhenAppServiceFails() {
        // Arrange
        // Symulujemy, że usługa aplikacji rzuca potężnym błędem (np. padła baza danych)
        doThrow(new RuntimeException("Baza danych zablokowana (Deadlock)"))
                .when(offerAppService).processExpiredOffers();

        // Act & Assert
        // W przypadku zadań w tle (Cron), adapter WEJŚCIOWY powinien sam złapać i zalogować wyjątek.
        // Gdyby rzucił go wyżej, mógłby zabić główny wątek Spring Schedulera
        // i zablokować inne zadania (np. wysyłkę e-maili)!
        assertThatCode(() -> cronJobAdapter.invalidateExpiredOffersJob())
                .doesNotThrowAnyException(); // Wymagamy, aby metoda NIE rzuciła wyjątku na zewnątrz

        verify(offerAppService, times(1)).processExpiredOffers();
    }
}