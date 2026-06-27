package salon.sales.infrastructure.in.messaging;

import org.springframework.stereotype.Component;
import salon.common.model.OrderId;
import salon.sales.application.domain.event.VehicleReadyForHandoverEvent;
import salon.sales.application.service.SalesService;

@Component
public class SalesEventSubscriberAdapter {

    private final SalesService salesAppService;

    public SalesEventSubscriberAdapter(SalesService salesAppService) {
        this.salesAppService = salesAppService;
    }

    
    public void onVehicleReadyForHandover(VehicleReadyForHandoverEvent event) {
        try {
            this.salesAppService.markOrderAsReadyForHandover(new OrderId(event.orderId()));
        } catch (RuntimeException ex) {
            
            System.err.println("[SalesEventSubscriberAdapter] Failed to handle VehicleReadyForHandover for order "
                    + event.orderId() + ": " + ex.getMessage());
        }
    }
}
