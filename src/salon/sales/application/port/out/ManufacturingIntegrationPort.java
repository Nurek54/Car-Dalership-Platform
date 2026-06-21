package salon.sales.application.port.out;

/**
 * Outbound port (driven) to the fulfillment systems (Inventory/factory):
 * an activated order triggers the vehicle fulfillment (a production order).
 */
public interface ManufacturingIntegrationPort {

    void startVehicleRealization(String orderId);
}
