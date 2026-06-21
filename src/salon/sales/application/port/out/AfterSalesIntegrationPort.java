package salon.sales.application.port.out;

/**
 * Outbound port (driven) to after-sales support: the handed-over vehicle is
 * registered in the service module (inspections, warranty).
 */
public interface AfterSalesIntegrationPort {

    void registerVehicleForAfterSales(String orderId);
}
