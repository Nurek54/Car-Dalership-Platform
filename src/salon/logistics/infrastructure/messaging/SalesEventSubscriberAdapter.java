package salon.logistics.infrastructure.messaging;

import salon.logistics.application.port.in.ReserveVehicleUseCase;
import salon.sales.domain.event.BankTransferDeclaredEvent;

/**
 * Adapter sterujący (driving) — subskrybent zdarzeń Kontekstu Sprzedaży/CRM
 * w Kontekście Inwentarza i Logistyki.
 *
 * UC-INW-01: BankTransferDeclared (klient zadeklarował przelew) potwierdza gotowość
 * do realizacji zamówienia i uruchamia weryfikację dostępności oraz rezerwację pojazdu.
 */
public class SalesEventSubscriberAdapter {

    private final ReserveVehicleUseCase reserveVehicle;

    public SalesEventSubscriberAdapter(ReserveVehicleUseCase reserveVehicle) {
        if (reserveVehicle == null) {
            throw new IllegalArgumentException("reserveVehicle must not be null.");
        }
        this.reserveVehicle = reserveVehicle;
    }

    public void handleBankTransferDeclared(BankTransferDeclaredEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        if (event.orderId() == null || event.orderId().isBlank()) {
            throw new IllegalArgumentException("Identyfikator zamówienia (orderId) jest wymagany");
        }
        this.reserveVehicle.reserveVehicleForOrder(event.orderId());
    }
}
