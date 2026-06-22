package salon.sales.application.port.in;

import salon.sales.application.command.ScheduleHandoverCommand;

public interface ScheduleHandover {

    void scheduleHandover(ScheduleHandoverCommand command);
}
