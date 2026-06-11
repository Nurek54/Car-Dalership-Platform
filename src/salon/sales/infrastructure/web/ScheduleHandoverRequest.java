package salon.sales.infrastructure.web;

import jakarta.validation.constraints.NotBlank;

/** DTO wejściowe UC-CRM-04: uzgodniona z klientem data odbioru pojazdu (ISO-8601). */
public record ScheduleHandoverRequest(
        @NotBlank(message = "Data odbioru jest wymagana") String handoverDate) {
}
