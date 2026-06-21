package salon.sales.application.handler;

import salon.sales.application.port.out.AfterSalesIntegrationPort;
import salon.sales.application.port.out.BillingIntegration;
import salon.sales.application.domain.event.VehicleHandedOverEvent;

/**
 * Handler of the VehicleHandedOver event (UC-CRM-05): after the vehicle handover it instructs the closing
 * of the balance in the Billing Context and registers the vehicle in after-sales support.
 */
public class VehicleHandedOverEventHandler {

    private final BillingIntegration billingPort;
    private final AfterSalesIntegrationPort afterSalesPort;

    public VehicleHandedOverEventHandler(BillingIntegration billingPort,
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
