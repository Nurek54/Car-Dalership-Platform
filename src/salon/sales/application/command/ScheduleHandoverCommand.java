package salon.sales.application.command;

import java.time.LocalDate;

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
