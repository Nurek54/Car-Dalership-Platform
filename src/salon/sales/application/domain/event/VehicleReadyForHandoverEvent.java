package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "VehicleReadyForHandover" — a message from the Inventory Context (UC-INW-05) received
 * by Sales/CRM: the vehicle with the given VIN is ready physically and financially,
 * the order is to transition to the "Ready for handover" state (UC-CRM-04).
 */
public record VehicleReadyForHandoverEvent(UUID eventId,
                                           String vin,
                                           String orderId,
                                           Instant occurredOn) implements DomainEvent {
}
