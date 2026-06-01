package salon.billing.application.port.in;

import java.math.BigDecimal;

/**
 * Komenda dla UC-ROZ-03.
 * Finansowanie i wartość odkupu pobieramy przez ExternalIntegrationPort — NIE ma ich w komendzie.
 */
public record CalculateSettlementCommand(String orderId,
                                         BigDecimal vehicleValue,
                                         BigDecimal totalDeposits,
                                         String currency) {

    public CalculateSettlementCommand {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        if (vehicleValue == null) {
            throw new IllegalArgumentException("vehicleValue must not be null.");
        }
        if (totalDeposits == null) {
            throw new IllegalArgumentException("totalDeposits must not be null.");
        }
        if (currency == null || currency.isBlank()) {
            throw new IllegalArgumentException("currency is required.");
        }
    }
}
