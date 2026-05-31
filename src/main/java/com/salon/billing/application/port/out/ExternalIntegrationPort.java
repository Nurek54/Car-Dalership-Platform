package main.java.com.salon.billing.application.port.out;

import main.java.com.salon.billing.domain.model.shared.Money;
import main.java.com.salon.billing.domain.model.shared.OrderId;

import java.util.Optional;

/**
 * Port wyjściowy do systemów zewnętrznych (m.in. Moduł Finansowania - ACL).
 * Wykorzystywany w UC-ROZ-03.
 */
public interface ExternalIntegrationPort {
    // Pusty Optional = finansowanie NIE zostało zatwierdzone (A2).
    Optional<Money> getApprovedFinancing(OrderId orderId);

    // Wartość odkupu pojazdu używanego (zero, jeśli brak odkupu).
    Money getTradeInValue(OrderId orderId);
}
