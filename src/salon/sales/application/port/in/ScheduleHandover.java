package salon.sales.application.port.in;

import salon.sales.application.command.ScheduleHandoverCommand;

/**
 * INBOUND PORT (Figure 22) — "ScheduleHandover".
 * UC-CRM-04: record the pickup date agreed with the customer.
 */
public interface ScheduleHandover {

    void scheduleHandover(ScheduleHandoverCommand command);
}
