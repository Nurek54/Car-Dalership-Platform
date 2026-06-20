package salon.billing.infrastructure.in.scheduling;

import salon.billing.application.port.in.ProcessPayment;

/**
 * ADAPTER STERUJACY (driving) — zadanie cykliczne przypomnien o niezaplaconych saldach.
 *
 * W kodzie produkcyjnym metoda ma nad soba np. @Scheduled(cron = "0 0 9 * * ?"). Adapter deleguje
 * do portu {@link ProcessPayment} i sam lapie wyjatki, by awaria pojedynczego uruchomienia nie
 * zatrzymala harmonogramu (cron odpali sie ponownie).
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
            System.err.println("[PaymentReminderCronJobAdapter] Wysylka przypomnien nie powiodla sie: "
                    + e.getMessage());
        }
    }
}
