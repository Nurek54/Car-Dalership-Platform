package salon.logistics.infrastructure.in.messaging;

import salon.logistics.application.port.in.ReserveVehicle;

/**
 * INBOUND ADAPTER (Figure 37: EventListener) – subscriber of the Financing Context events.
 *
 * After confirmation of readiness to fulfill the order (FinancingApproved / BankTransferDeclared)
 * it triggers UC-INW-01 (reservation of a vehicle from the yard). Anti-corruption layer (ACL):
 * we represent external messages as local records and translate them into a port call.
 */
public class FinancingEventSubscriberAdapter {

    private final ReserveVehicle reserveVehicle;

    public FinancingEventSubscriberAdapter(ReserveVehicle reserveVehicle) {
        if (reserveVehicle == null) {
            throw new IllegalArgumentException("reserveVehicle must not be null.");
        }
        this.reserveVehicle = reserveVehicle;
    }

    public void handleFinancingApproved(FinancingApproved event) {
        requireOrderId(event == null ? null : event.orderId());
        this.reserveVehicle.reserveVehicleForOrder(event.orderId());
    }

    public void handleBankTransferDeclared(BankTransferDeclared event) {
        requireOrderId(event == null ? null : event.orderId());
        this.reserveVehicle.reserveVehicleForOrder(event.orderId());
    }

    private static void requireOrderId(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
    }

    /** Local (ACL) representations of events incoming from the Financing Context. */
    public record FinancingApproved(String orderId) {
    }

    public record BankTransferDeclared(String orderId) {
    }
}
