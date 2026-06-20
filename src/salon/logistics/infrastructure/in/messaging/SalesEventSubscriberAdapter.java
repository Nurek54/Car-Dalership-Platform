package salon.logistics.infrastructure.in.messaging;

import salon.logistics.application.port.in.ReleaseVehicle;
import salon.logistics.application.port.out.CatalogIntegration;

/**
 * ADAPTER WEJŚCIOWY (Rysunek 37: EventListener) – subskrybent zdarzeń/komend Kontekstu Sprzedaży i CRM.
 *
 * Obsługuje dwie ścieżki:
 *  - OrderPlaced -> powiązanie zamówienia z jego specyfikacją w lokalnej kopii danych Katalogu,
 *  - ReleaseVehicle (komenda) -> UC-INW-06 (zdjęcie pojazdu ze stanu po wydaniu).
 */
public class SalesEventSubscriberAdapter {

    private final CatalogIntegration catalogIntegration;
    private final ReleaseVehicle releaseVehicle;

    public SalesEventSubscriberAdapter(CatalogIntegration catalogIntegration,
                                       ReleaseVehicle releaseVehicle) {
        if (catalogIntegration == null) {
            throw new IllegalArgumentException("catalogIntegration must not be null.");
        }
        if (releaseVehicle == null) {
            throw new IllegalArgumentException("releaseVehicle must not be null.");
        }
        this.catalogIntegration = catalogIntegration;
        this.releaseVehicle = releaseVehicle;
    }

    public void handleOrderPlaced(OrderPlaced event) {
        if (event == null || event.orderId() == null || event.specificationId() == null) {
            throw new IllegalArgumentException("orderId and specificationId must not be null.");
        }
        this.catalogIntegration.linkOrderToSpecification(event.orderId(), event.specificationId());
    }

    public void handleReleaseVehicle(ReleaseVehicleCommand command) {
        if (command == null || command.orderId() == null || command.orderId().isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        this.releaseVehicle.releaseVehicle(command.orderId());
    }

    /** Lokalne (ACL) reprezentacje komunikatów z Kontekstu Sprzedaży i CRM. */
    public record OrderPlaced(String orderId, String specificationId) {
    }

    public record ReleaseVehicleCommand(String orderId) {
    }
}
