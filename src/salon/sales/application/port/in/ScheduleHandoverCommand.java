package salon.sales.application.port.in;

import java.time.LocalDate;

/**
 * Komenda dla UC-CRM-04 (krok 4-5): Handlowiec wprowadza uzgodniony z klientem termin odbioru.
 * Obsługuje też scenariusz A1 (odroczony odbiór) — wystarczy późniejsza data.
 */
public record ScheduleHandoverCommand(String orderId, LocalDate handoverDate) {

    public ScheduleHandoverCommand {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        if (handoverDate == null) {
            throw new IllegalArgumentException("handoverDate must not be null.");
        }
    }
}
