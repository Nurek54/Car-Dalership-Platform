package salon.sales.application.port.out;

/**
 * OUTBOUND PORT — "ManufacturingIntegration". On order activation (UC-CRM-03 part 2) starts the
 * vehicle realization (factory order onto the production line).
 */
public interface ManufacturingIntegrationPort {

    void startVehicleRealization(String orderId);
}
