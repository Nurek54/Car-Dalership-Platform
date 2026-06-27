package salon.logistics.infrastructure.in.messaging;

import salon.logistics.application.port.in.ReserveVehicle;

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

    
    public record FinancingApproved(String orderId) {
    }

    public record BankTransferDeclared(String orderId) {
    }
}
