package salon.billing.infrastructure.mock;

import salon.billing.application.port.out.ExternalIntegrationPort;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Udawana integracja zewnętrzna. Zwraca finansowanie 50 000 PLN i zerowy odkup,
 * dzięki czemu test end-to-end daje saldo 30 000 PLN i stan SETTLED.
 */
public class ExternalIntegrationMockAdapter implements ExternalIntegrationPort {

    @Override
    public Optional<Money> getApprovedFinancing(OrderId orderId) {
        return Optional.of(new Money(new BigDecimal("50000"), "PLN"));
    }

    @Override
    public Money getTradeInValue(OrderId orderId) {
        return new Money(BigDecimal.ZERO, "PLN");
    }
}
