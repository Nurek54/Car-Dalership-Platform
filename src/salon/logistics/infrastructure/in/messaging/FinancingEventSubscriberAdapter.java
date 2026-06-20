package salon.logistics.infrastructure.in.messaging;

import salon.logistics.application.port.in.ReserveVehicle;

/**
 * ADAPTER WEJŚCIOWY (Rysunek 37: EventListener) – subskrybent zdarzeń Kontekstu Finansowania.
 *
 * Po potwierdzeniu gotowości do realizacji zamówienia (FinancingApproved / BankTransferDeclared)
 * uruchamia UC-INW-01 (rezerwacja pojazdu z placu). Warstwa zapobiegająca uszkodzeniu (ACL):
 * komunikaty zewnętrzne reprezentujemy jako lokalne rekordy i tłumaczymy na wywołanie portu.
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

    /** Lokalne (ACL) reprezentacje zdarzeń przychodzących z Kontekstu Finansowania. */
    public record FinancingApproved(String orderId) {
    }

    public record BankTransferDeclared(String orderId) {
    }
}
