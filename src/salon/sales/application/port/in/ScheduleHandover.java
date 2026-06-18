package salon.sales.application.port.in;

import salon.sales.application.command.ScheduleHandoverCommand;

/**
 * Port wejściowy dla UC-CRM-04: ustalenie terminu odbioru pojazdu przez Handlowca.
 */
public interface ScheduleHandover {
    void scheduleHandover(ScheduleHandoverCommand command);
}
