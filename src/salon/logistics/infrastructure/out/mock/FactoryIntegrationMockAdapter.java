package salon.logistics.infrastructure.out.mock;

import salon.logistics.application.port.out.FactoryIntegrationAclPort;
import salon.logistics.application.domain.model.vehicle.ImporterData;
import salon.logistics.application.domain.model.vehicle.VinNumber;
import salon.common.model.OrderId;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Adapter wyjściowy (mock, ACL) portu FactoryIntegrationAclPort — symuluje API
 * producenta/importera. W środowisku docelowym zastępuje go klient HTTP pełniący
 * rolę warstwy zapobiegającej uszkodzeniom (Anti-Corruption Layer).
 */
public class FactoryIntegrationMockAdapter implements FactoryIntegrationAclPort {

    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public VinNumber placeFactoryOrder(OrderId orderId, List<String> specCodes) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        String vin = String.format("WVWZZZ1JZ%08d", this.sequence.getAndIncrement());
        System.out.println("[FactoryIntegrationMock] PlaceFactoryOrder(" + orderId.value()
                + ", " + specCodes + ") -> acknowledged, VIN=" + vin);
        return new VinNumber(vin);
    }

    @Override
    public ImporterData fetchVehicleData(VinNumber vin) {
        if (vin == null) {
            throw new IllegalArgumentException("vin must not be null.");
        }
        System.out.println("[FactoryIntegrationMock] fetchVehicleData(" + vin.value() + ")");
        return new ImporterData(vin.value());
    }
}
