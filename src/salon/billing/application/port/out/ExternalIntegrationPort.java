package salon.billing.application.port.out;

import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.util.Optional;

/**
 * Port wyjściowy do systemów zewnętrznych (m.in. Moduł Finansowania - ACL). UC-ROZ-03.
 */
public interface ExternalIntegrationPort {
    // Pusty Optional = finansowanie NIE zostało zatwierdzone (A2).
    Optional<Money> getApprovedFinancing(OrderId orderId);

    // Wartość odkupu pojazdu używanego (zero, jeśli brak odkupu).
    Money getTradeInValue(OrderId orderId);
}
