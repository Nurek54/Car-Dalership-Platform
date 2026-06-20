package salon.logistics.infrastructure.in.messaging;

import salon.logistics.application.port.in.ReserveVehicle;
import salon.logistics.application.port.in.SynchronizeSpecificationUseCase;
import salon.sales.application.domain.event.BankTransferDeclaredEvent;
import salon.sales.application.domain.event.OrderPlacedEvent;

/**
 * Adapter sterujący (driving) — subskrybent zdarzeń Kontekstu Sprzedaży/CRM
 * w Kontekście Inwentarza i Logistyki.
 *
 * OrderPlaced (zamówienie złożone) niesie specificationId — Inwentarz wiąże
 * zamówienie ze specyfikacją w lokalnym read modelu (event-carried state transfer).
 *
 * UC-INW-01: BankTransferDeclared (klient zadeklarował przelew) potwierdza gotowość
 * do realizacji zamówienia i uruchamia weryfikację dostępności oraz rezerwację pojazdu.
 */
public class SalesEventSubscriberAdapter {

    private final ReserveVehicle reserveVehicle;
    private final SynchronizeSpecificationUseCase synchronizeSpecification;

    public SalesEventSubscriberAdapter(ReserveVehicle reserveVehicle,
                                       SynchronizeSpecificationUseCase synchronizeSpecification) {
        if (reserveVehicle == null) {
            throw new IllegalArgumentException("reserveVehicle must not be null.");
        }
        if (synchronizeSpecification == null) {
            throw new IllegalArgumentException("synchronizeSpecification must not be null.");
        }
        this.reserveVehicle = reserveVehicle;
        this.synchronizeSpecification = synchronizeSpecification;
    }

    /** Zamówienie złożone -> powiązanie orderId ze specificationId w read modelu Inwentarza. */
    public void handleOrderPlaced(OrderPlacedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        if (event.orderId() == null || event.orderId().isBlank()) {
            throw new IllegalArgumentException("Identyfikator zamówienia (orderId) jest wymagany");
        }
        if (event.specificationId() == null || event.specificationId().isBlank()) {
            System.out.println("[SalesEventSubscriberAdapter] OrderPlaced bez specificationId — "
                    + "pomijam powiązanie dla zamówienia " + event.orderId());
            return;
        }
        this.synchronizeSpecification.linkOrderToSpecification(
                event.orderId(), event.specificationId());
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
