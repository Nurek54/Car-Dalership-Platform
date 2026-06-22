package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * Inbound (Inventory/Logistics) representation consumed by Sales: the physical vehicle (by VIN)
 * arrived and is ready for handover for the given order (UC-CRM-04).
 */
public record VehicleReadyForHandoverEvent(String vin, String orderId,
                                           UUID eventId, Instant occurredOn) implements DomainEvent {

    public VehicleReadyForHandoverEvent(String vin, String orderId) {
        this(vin, orderId, UUID.randomUUID(), Instant.now());
    }

    public VehicleReadyForHandoverEvent(UUID eventId, String vin, String orderId, Instant occurredOn) {
        this(vin, orderId, eventId, occurredOn);
    }
}
