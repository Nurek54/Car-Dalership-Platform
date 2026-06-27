package salon.logistics.infrastructure.out.mock;

import salon.logistics.application.port.out.ImporterACL;

import java.util.List;
import java.util.UUID;

public class FactoryIntegrationMockAdapter implements ImporterACL {

    @Override
    public String placeFactoryOrder(String orderId, List<String> optionCodes) {
        String vin = "VIN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        System.out.println("[FactoryIntegrationMockAdapter] Production order for " + orderId
                + " accepted by the factory, assigned VIN=" + vin + ".");
        return vin;
    }
}
