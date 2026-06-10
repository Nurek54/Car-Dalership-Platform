package salon.billing.infrastructure.messaging;

import java.time.Instant;
import java.util.UUID;

/**
 * Kontrakt integracyjny zdarzenia "PojazduBrakNaStocku" z Inwentarza (Long Track) —
 * trigger UC-FIR-01: prośba o zadatek dla zamówienia czekającego na produkcję.
 */
public record VehicleIsNotOnStockEvent(UUID eventId,
                                       String orderId,
                                       Instant occurredOn) {
}
