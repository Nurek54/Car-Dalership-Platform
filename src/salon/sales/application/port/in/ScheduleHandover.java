package salon.sales.application.port.in;

import salon.sales.application.command.ScheduleHandoverCommand;

/**
 * Inbound port for UC-CRM-04: the Salesperson setting the vehicle pickup date.
 */
public interface ScheduleHandover {
    void scheduleHandover(ScheduleHandoverCommand command);
}
