package integration.driving_adapetrs.cron_job_adapter.billing;

import salon.billing.application.service.PaymentAppService;

/**
 * Adapter sterujący (driving) — zadanie cykliczne wysyłki przypomnień o niepełnych wpłatach.
 *
 * W kodzie produkcyjnym metoda ma nad sobą np. @Scheduled(cron = "0 0 8 * * ?") (np. o 8:00 rano).
 * Adapter deleguje do warstwy aplikacji i sam łapie wyjątki (np. brak odpowiedzi serwera SMTP),
 * żeby awaria pojedynczego uruchomienia nie zatrzymała całego Schedulera.
 */
public class PaymentReminderCronJobAdapter {

    private final PaymentAppService paymentAppService;

    public PaymentReminderCronJobAdapter(PaymentAppService paymentAppService) {
        if (paymentAppService == null) {
            throw new IllegalArgumentException("paymentAppService must not be null.");
        }
        this.paymentAppService = paymentAppService;
    }

    // Wyzwalane przez harmonogram: rozeslij przypomnienia o zaległych/niepełnych wpłatach.
    public void sendRemindersJob() {
        try {
            paymentAppService.processPaymentReminders();
        } catch (Exception e) {
            // Połykamy i logujemy — inaczej wyjątek ubije wątek Spring Schedulera.
            System.err.println("[PaymentReminderCronJobAdapter] Wysyłka przypomnień nie powiodła się: "
                    + e.getMessage());
        }
    }
}
