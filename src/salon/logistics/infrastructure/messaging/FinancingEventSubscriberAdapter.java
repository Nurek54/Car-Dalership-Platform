package salon.logistics.infrastructure.messaging;

import salon.financing.domain.event.FinancingApprovedEvent;
import salon.logistics.application.port.in.ReserveVehicleUseCase;

/**
 * Adapter sterujący (driving) — subskrybent zdarzeń Kontekstu Finansowania
 * w Kontekście Inwentarza i Logistyki.
 *
 * UC-INW-01: FinancingApproved (bank przyznał finansowanie) potwierdza gotowość
 * do realizacji zamówienia i uruchamia weryfikację dostępności oraz rezerwację pojazdu.
 */
public class FinancingEventSubscriberAdapter {

    private final ReserveVehicleUseCase reserveVehicle;

    public FinancingEventSubscriberAdapter(ReserveVehicleUseCase reserveVehicle) {
        if (reserveVehicle == null) {
            throw new IllegalArgumentException("reserveVehicle must not be null.");
        }
        this.reserveVehicle = reserveVehicle;
    }

    public void handleFinancingApproved(FinancingApprovedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        if (event.orderId() == null || event.orderId().isBlank()) {
            throw new IllegalArgumentException("Identyfikator zamówienia (orderId) jest wymagany");
        }
        this.reserveVehicle.reserveVehicleForOrder(event.orderId());
    }
}
