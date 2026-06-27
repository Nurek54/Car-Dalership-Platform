package salon.sales.application.handler;

import salon.sales.application.domain.event.VehicleHandedOverEvent;
import salon.sales.application.port.out.AfterSalesIntegrationPort;
import salon.sales.application.port.out.BillingIntegration;

public class VehicleHandedOverEventHandler {

    private final BillingIntegration billingPort;
    private final AfterSalesIntegrationPort afterSalesPort;

    public VehicleHandedOverEventHandler(BillingIntegration billingPort, AfterSalesIntegrationPort afterSalesPort) {
        this.billingPort = billingPort;
        this.afterSalesPort = afterSalesPort;
    }

    public void handle(VehicleHandedOverEvent event) {
        
        this.billingPort.closeOrderBalance(event.orderId());
        this.afterSalesPort.openServiceWindow(event.orderId());
    }
}
