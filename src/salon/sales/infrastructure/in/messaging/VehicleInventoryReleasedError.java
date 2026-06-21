package salon.sales.infrastructure.in.messaging;

/**
 * INCOMING (integration) event from Inventory (UC-CRM-05, A1): failed to release
 * the vehicle from stock (e.g. a stock lock). It lives in the integration layer, so the Sales
 * domain stays untouched. eventId is used for deduplication on the subscriber side.
 */
public record VehicleInventoryReleasedError(String eventId, String orderId, String reason) {
}
