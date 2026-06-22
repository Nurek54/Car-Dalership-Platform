package salon.billing.infrastructure.in.scheduling;

import salon.billing.application.port.in.ProcessPayment;

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
