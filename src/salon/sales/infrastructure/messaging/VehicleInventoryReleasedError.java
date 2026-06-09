package salon.sales.infrastructure.messaging;

/**
 * Zdarzenie PRZYCHODZĄCE (integracyjne) z Inwentarza (UC-CRM-05, A1): nie udało się zwolnić
 * pojazdu z magazynu (np. blokada magazynowa). Żyje w warstwie integracyjnej, więc domena
 * Sprzedaży pozostaje nietknięta. eventId służy do deduplikacji po stronie subskrybenta.
 */
public record VehicleInventoryReleasedError(String eventId, String orderId, String reason) {
}
