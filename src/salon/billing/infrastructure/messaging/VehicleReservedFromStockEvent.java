package salon.billing.infrastructure.messaging;

import java.time.Instant;
import java.util.UUID;

/**
 * Kontrakt integracyjny zdarzenia "PojazdZarezerwowanyZeStocku" wpadającego z Inwentarza
 * do Kontekstu Fakturowania (trigger UC-FIR-02). Niesie WYŁĄCZNIE techniczne identyfikatory
 * (orderId, vin) — dane nabywcy Fakturowanie dociąga przez CrmIntegrationPort.
 */
public record VehicleReservedFromStockEvent(UUID eventId,
                                            String orderId,
                                            String vin,
                                            Instant occurredOn) {
}
