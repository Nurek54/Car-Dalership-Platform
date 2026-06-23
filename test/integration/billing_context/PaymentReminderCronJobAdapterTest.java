package integration.billing_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import salon.billing.application.port.in.ProcessPayment;
import salon.billing.infrastructure.in.scheduling.PaymentReminderCronJobAdapter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

/** Adapter CronJob — cykliczne wysyłanie przypomnień o płatności (UC-FIR-03, wsparcie). */
@SpringBootTest(classes = PaymentReminderCronJobAdapter.class)
class PaymentReminderCronJobAdapterTest {

    @Autowired private PaymentReminderCronJobAdapter adapter;
    @MockBean private ProcessPayment processPayment;

    @Test
    void shouldTriggerReminders() {
        adapter.sendPaymentRemindersJob();

        verify(processPayment).sendPaymentReminders();
    }

    @Test
    void shouldSwallowErrorsSoCronKeepsRunning() {
        // Błąd zadania okresowego jest przechwytywany — cron nie wysadza całego procesu
        doThrow(new RuntimeException("Baza niedostępna")).when(processPayment).sendPaymentReminders();

        assertDoesNotThrow(() -> adapter.sendPaymentRemindersJob());
    }
}
