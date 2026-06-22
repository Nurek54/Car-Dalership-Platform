package salon.sales.infrastructure.in.web;

import salon.sales.application.command.ScheduleHandoverCommand;
import salon.sales.application.port.in.ActivateOrderOnDeposit;
import salon.sales.application.port.in.ReleaseVehicle;
import salon.sales.application.port.in.ScheduleHandover;

import java.time.LocalDate;

/**
 * INBOUND ADAPTER (Figure 22: RestController) — drives the order-lifecycle ports:
 * {@link ActivateOrderOnDeposit}, {@link ScheduleHandover} and {@link ReleaseVehicle}
 * (UC-CRM-03 part 2 / UC-CRM-05).
 */
public class OrderRestController {

    private final ActivateOrderOnDeposit activateOrderOnDeposit;
    private final ScheduleHandover scheduleHandover;
    private final ReleaseVehicle releaseVehicle;

    public OrderRestController(ActivateOrderOnDeposit activateOrderOnDeposit,
                               ScheduleHandover scheduleHandover,
                               ReleaseVehicle releaseVehicle) {
        if (activateOrderOnDeposit == null || scheduleHandover == null || releaseVehicle == null) {
            throw new IllegalArgumentException("OrderRestController dependencies must not be null.");
        }
        this.activateOrderOnDeposit = activateOrderOnDeposit;
        this.scheduleHandover = scheduleHandover;
        this.releaseVehicle = releaseVehicle;
    }

    public void activateOnDeposit(String orderId) {
        this.activateOrderOnDeposit.activateOnDeposit(orderId);
    }

    public void scheduleHandover(String orderId, LocalDate handoverDate) {
        this.scheduleHandover.scheduleHandover(new ScheduleHandoverCommand(orderId, handoverDate));
    }

    public void releaseVehicle(String orderId) {
        this.releaseVehicle.releaseVehicle(orderId);
    }
}
