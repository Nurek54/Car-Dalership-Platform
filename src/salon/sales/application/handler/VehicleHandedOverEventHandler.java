package salon.sales.application.handler;

import salon.sales.application.port.out.AfterSalesIntegrationPort;
import salon.sales.application.port.out.BillingIntegrationPort;
import salon.sales.domain.event.VehicleHandedOverEvent;

/**
 * Handler zdarzenia VehicleHandedOver (UC-CRM-05): po wydaniu pojazdu zleca domknięcie
 * salda w Kontekście Rozliczeń i rejestruje pojazd w obsłudze posprzedażowej.
 */
public class VehicleHandedOverEventHandler {

    private final BillingIntegrationPort billingPort;
    private final AfterSalesIntegrationPort afterSalesPort;

    public VehicleHandedOverEventHandler(BillingIntegrationPort billingPort,
                                         AfterSalesIntegrationPort afterSalesPort) {
        this.billingPort = billingPort;
        this.afterSalesPort = afterSalesPort;
    }

    public void handle(VehicleHandedOverEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        this.billingPort.closeOrderBalance(event.orderId());
        if (this.afterSalesPort != null) {
            this.afterSalesPort.registerVehicleForAfterSales(event.orderId());
        }
    }
}
