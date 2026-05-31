package main.java.com.salon.billing.infrastructure.mock;

import main.java.com.salon.billing.application.port.out.ExternalIntegrationPort;
import main.java.com.salon.billing.domain.model.shared.Money;
import main.java.com.salon.billing.domain.model.shared.OrderId;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Adapter do systemów zewnętrznych (Moduł Finansowania - ACL) — wersja MOCK.
 * Zwraca przykładowe, sztywne dane na potrzeby developmentu.
 */
public class ExternalIntegrationMockAdapter implements ExternalIntegrationPort {

    @Override
    public Optional<Money> getApprovedFinancing(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        // Mock: udajemy zatwierdzone finansowanie 50 000 PLN.
        // Zwróć Optional.empty(), aby przetestować scenariusz A2 (brak finansowania).
        return Optional.of(new Money(new BigDecimal("50000.00"), "PLN"));
    }

    @Override
    public Money getTradeInValue(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        // Mock: brak odkupu -> wartość zero.
        return new Money(BigDecimal.ZERO, "PLN");
    }
}
