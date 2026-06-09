package salon.sales.application.port.out;

import salon.sales.domain.model.order.Order;
import salon.shared.model.SpecificationId;

/**
 * Port wyjściowy (driven) integracji z Kontekstem Inwentarza/Logistyki —
 * węzeł "InventoryIntegrationPort" w docs/Architecture/SalesArchitecture.md.
 *
 * Sprzedaż prosi Inwentarz o alokację/rezerwację pojazdu pod złożone zamówienie
 * (np. po zdarzeniu OrderPlaced). Konkretną komunikację (REST/komunikat) realizuje adapter.
 */
public interface InventoryIntegrationPort {

    /**
     * Zleca alokację pojazdu zgodnego ze specyfikacją dla danego zamówienia.
     */
    void requestVehicleAllocation(Order order, SpecificationId specificationId);
}
