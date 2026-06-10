package salon.logistics.infrastructure.mock;

import salon.logistics.application.port.out.SpecificationIntegrationPort;
import salon.shared.model.OrderId;

import java.util.List;

/**
 * Adapter wyjściowy (mock) portu SpecificationIntegrationPort — zwraca przykładowe
 * kody wyposażenia. W środowisku docelowym zastąpi go adapter wołający Kontekst
 * Katalogu/Sprzedaży (REST/komunikat).
 */
public class SpecificationIntegrationMockAdapter implements SpecificationIntegrationPort {

    @Override
    public List<String> getSpecificationForOrder(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        System.out.println("[SpecificationIntegrationMock] getSpecificationForOrder("
                + orderId.value() + ")");
        return List.of("ENG-HYBRID", "COL-RED", "PKG-COMFORT");
    }
}
