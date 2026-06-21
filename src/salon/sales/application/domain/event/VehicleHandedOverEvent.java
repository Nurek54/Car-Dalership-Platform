package salon.sales.application.domain.event;

import salon.common.event.DomainEvent;

import java.time.Instant;
import java.util.UUID;

/**
 * "VehicleHandedOver" (UC-CRM-05) — the customer picked up the vehicle from the dealership.
 * Listeners: the Billing Context (closing the balance — the VehicleHandedOverEventHandler)
 * and after-sales support.
 */
public record VehicleHandedOverEvent(UUID eventId,
                                     String orderId,
                                     Instant occurredOn) implements DomainEvent {
}
