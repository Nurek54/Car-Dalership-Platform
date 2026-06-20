package salon.logistics.infrastructure.out.mock;

import salon.logistics.application.port.out.ImporterACL;

import java.util.List;
import java.util.UUID;

/**
 * ADAPTER WYJŚCIOWY (ACL, Rysunek 37: ImporterACL) – atrapa integracji z systemem fabryki.
 *
 * Symuluje synchroniczne złożenie zlecenia produkcji: zwraca nadany przez fabrykę numer VIN.
 * Wariant zgłaszający {@code FactoryOrderFailedException} reprezentowałby scenariusz UC-INW-02 / A1.
 */
public class FactoryIntegrationMockAdapter implements ImporterACL {

    @Override
    public String placeFactoryOrder(String orderId, List<String> optionCodes) {
        String vin = "VIN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        System.out.println("[FactoryIntegrationMockAdapter] Zlecenie produkcji dla " + orderId
                + " przyjęte przez fabrykę, nadany VIN=" + vin + ".");
        return vin;
    }
}
