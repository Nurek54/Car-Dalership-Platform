package integration.driving_adapetrs.cron_job_adapter.billing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.billing.application.service.PaymentAppService;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentReminderCronJobAdapterTest {

    // Kaskader dla usługi aplikacji w Kontekście Rozliczeń
    @Mock
    private PaymentAppService paymentAppService;

    // Testowany adapter
    @InjectMocks
    private PaymentReminderCronJobAdapter adapter;

    // 1. HAPPY PATH: Prawidłowe uruchomienie procesu wysyłki przypomnień
    @Test
    void shouldTriggerSendingPaymentReminders() {
        // Act
        // Symulujemy moment, w którym Spring odpala metodę (np. o 8:00 rano)
        adapter.sendRemindersJob();

        // Assert
        // Upewniamy się, że adapter przekazał zadanie do jądra systemu
        verify(paymentAppService, times(1)).processPaymentReminders();
    }

    // 2. NIEZAWODNOŚĆ
    @Test
    void shouldNotBubbleUpExceptionWhenAppServiceFails() {
        // Arrange
        // Symulujemy awarię w logice biznesowej (np. serwer SMTP od e-maili nie odpowiada)
        doThrow(new RuntimeException("Błąd połączenia z serwerem SMTP"))
                .when(paymentAppService).processPaymentReminders();

        // Act & Assert
        // Adapter ZAWSZE musi złapać wyjątek (try-catch) i go zalogować.
        // Jeśli wyrzuci go na zewnątrz, Spring zamknie ten wątek Schedulera!
        assertThatCode(() -> adapter.sendRemindersJob())
                .doesNotThrowAnyException();

        verify(paymentAppService, times(1)).processPaymentReminders();
    }
}