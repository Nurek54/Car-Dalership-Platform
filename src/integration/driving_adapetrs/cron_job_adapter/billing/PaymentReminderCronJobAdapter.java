package integration.driving_adapetrs.cron_job_adapter.billing;

import salon.billing.application.service.SettlementAppService;

/**
 * Adapter sterujący (driving) — zadanie cykliczne wysyłki przypomnień o niepełnych wpłatach.
 *
 * W kodzie produkcyjnym metoda ma nad sobą np. @Scheduled(cron = "0 0 8 * * ?") (np. o 8:00 rano).
 * Adapter deleguje do warstwy aplikacji i sam łapie wyjątki (np. brak odpowiedzi serwera SMTP),
 * żeby awaria pojedynczego uruchomienia nie zatrzymała całego Schedulera.
 */
public class PaymentReminderCronJobAdapter {

    private final SettlementAppService settlementAppService;

    public PaymentReminderCronJobAdapter(SettlementAppService settlementAppService) {
        if (settlementAppService == null) {
            throw new IllegalArgumentException("settlementAppService must not be null.");
        }
        this.settlementAppService = settlementAppService;
    }

    // Wyzwalane przez harmonogram: rozeslij przypomnienia o zaległych/niepełnych wpłatach.
    public void sendRemindersJob() {
        try {
            settlementAppService.processPaymentReminders();
        } catch (Exception e) {
            // Połykamy i logujemy — inaczej wyjątek ubije wątek Spring Schedulera.
            System.err.println("[PaymentReminderCronJobAdapter] Wysyłka przypomnień nie powiodła się: "
                    + e.getMessage());
        }
    }
}
