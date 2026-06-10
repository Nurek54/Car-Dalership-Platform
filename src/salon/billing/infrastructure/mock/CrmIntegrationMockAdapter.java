package salon.billing.infrastructure.mock;

import salon.billing.application.port.out.CrmIntegrationPort;
import salon.billing.domain.model.document.BuyerDetails;
import salon.shared.model.OrderId;

/**
 * Adapter wyjściowy (mock) portu CrmIntegrationPort — zwraca przykładowe dane nabywcy.
 * W środowisku docelowym zastąpi go adapter wołający moduł Sprzedaży/CRM (REST/komunikat).
 */
public class CrmIntegrationMockAdapter implements CrmIntegrationPort {

    @Override
    public BuyerDetails getCustomerDetails(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        System.out.println("[CrmIntegrationMock] getCustomerDetails(" + orderId.value() + ")");
        return new BuyerDetails("Jan Kowalski", "1234567890");
    }
}
