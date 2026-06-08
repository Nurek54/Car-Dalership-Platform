package integration.driving_adapetrs.cron_job_adapter.billing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.billing.application.service.SettlementAppService;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentReminderCronJobAdapterTest {

    // Kaskader dla usługi aplikacji w Kontekście Fakturowania i Rozliczeń
    @Mock
    private SettlementAppService settlementAppService;

    // Testowany adapter
    @InjectMocks
    private PaymentReminderCronJobAdapter adapter;

    // 1. HAPPY PATH: Prawidłowe uruchomienie procesu wysyłki przypomnień
    @Test
    void shouldTriggerSendingPaymentReminders() {
        adapter.sendRemindersJob();
        verify(settlementAppService, times(1)).processPaymentReminders();
    }

    // 2. NIEZAWODNOŚĆ: adapter ZAWSZE łapie wyjątek (try-catch) i go loguje
    @Test
    void shouldNotBubbleUpExceptionWhenAppServiceFails() {
        doThrow(new RuntimeException("Błąd połączenia z serwerem SMTP"))
                .when(settlementAppService).processPaymentReminders();

        assertThatCode(() -> adapter.sendRemindersJob())
                .doesNotThrowAnyException();

        verify(settlementAppService, times(1)).processPaymentReminders();
    }
}
