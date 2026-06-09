package salon.logistics.application.port.in;

import java.util.List;

/**
 * Port wejściowy kontekstu Inwentarza i Logistyki (UC-INW-01, 07).
 */
public interface ManageInventoryUseCase {
    void receiveVehicle(String vin);
    void allocateVehicleForOrder(String orderId, List<String> specCodes);
}
