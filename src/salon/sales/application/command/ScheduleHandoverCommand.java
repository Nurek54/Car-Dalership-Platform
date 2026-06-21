package salon.sales.application.command;

import java.time.LocalDate;

/**
 * Command for UC-CRM-04 (steps 4-5): the Salesperson enters the handover date agreed with the customer.
 * It also supports scenario A1 (deferred pickup) — a later date is enough.
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
