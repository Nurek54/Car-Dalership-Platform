package salon.billing.infrastructure.in.scheduling;

import salon.billing.application.port.in.ProcessPayment;

/**
 * DRIVING ADAPTER — periodic task for reminders about unpaid balances.
 *
 * W kodzie produkcyjnym metoda ma nad soba np. @Scheduled(cron = "0 0 9 * * ?"). Adapter deleguje
 * to the {@link ProcessPayment} port and catches exceptions itself, so that a single run's failure does not
 * stop the scheduler (the cron will fire again).
 */
public class PaymentReminderCronJobAdapter {

    private final ProcessPayment processPayment;

    public PaymentReminderCronJobAdapter(ProcessPayment processPayment) {
        if (processPayment == null) {
            throw new IllegalArgumentException("processPayment must not be null.");
        }
        this.processPayment = processPayment;
    }

    public void sendPaymentRemindersJob() {
        try {
            this.processPayment.sendPaymentReminders();
        } catch (Exception e) {
            System.err.println("[PaymentReminderCronJobAdapter] Sending reminders failed: "
                    + e.getMessage());
        }
    }
}
