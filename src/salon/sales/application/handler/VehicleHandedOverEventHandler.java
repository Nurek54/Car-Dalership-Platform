package salon.sales.application.handler;

import salon.sales.application.domain.event.VehicleHandedOverEvent;
import salon.sales.application.port.out.AfterSalesIntegrationPort;
import salon.sales.application.port.out.BillingIntegration;

/**
 * EVENT HANDLER (Figure 22) — reacts to {@link VehicleHandedOverEvent} (UC-CRM-05):
 * tells Billing to close the balance and After-sales to open the service window.
 */
public class VehicleHandedOverEventHandler {

    private final BillingIntegration billingPort;
    private final AfterSalesIntegrationPort afterSalesPort;

    public VehicleHandedOverEventHandler(BillingIntegration billingPort, AfterSalesIntegrationPort afterSalesPort) {
        this.billingPort = billingPort;
        this.afterSalesPort = afterSalesPort;
    }

    public void handle(VehicleHandedOverEvent event) {
        // We instruct the Billing context to close the balance
        this.billingPort.closeOrderBalance(event.orderId());
        this.afterSalesPort.openServiceWindow(event.orderId());
    }
}
