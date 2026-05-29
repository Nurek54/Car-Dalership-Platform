package main.java.com.salon.billing.application.port.in;

import java.math.BigDecimal;

/**
 * Komenda dla UC-ROZ-03.
 * Uwaga: finansowanie i wartość odkupu pobieramy przez ExternalIntegrationPort,
 * dlatego NIE ma ich w komendzie.
 */
public record CalculateSettlementCommand(String orderId,
                                         BigDecimal vehicleValue,
                                         BigDecimal totalDeposits,
                                         String currency) {
}