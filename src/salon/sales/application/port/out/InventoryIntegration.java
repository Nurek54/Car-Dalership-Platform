package salon.sales.application.port.out;

public interface InventoryIntegration {

    
    void releaseVehicle(String orderId);

    
    void releasePhysicalVehicle(String orderId);

    
    void allocateVehicleOrProductionSlot(String orderId);
}
