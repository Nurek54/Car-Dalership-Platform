package salon.sales.infrastructure.in.web;

import jakarta.validation.constraints.NotBlank;

/** Input DTO for UC-CRM-04: the vehicle pickup date agreed with the customer (ISO-8601). */
public record ScheduleHandoverRequest(
        @NotBlank(message = "The handover date is required") String handoverDate) {
}
