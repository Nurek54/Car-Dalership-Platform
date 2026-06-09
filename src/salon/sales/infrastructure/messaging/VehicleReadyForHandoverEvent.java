package salon.sales.infrastructure.messaging;

/**
 * Zdarzenie PRZYCHODZĄCE (integracyjne) z Kontekstu Inwentarza i Logistyki (UC-CRM-04):
 * pojazd jest gotowy fizycznie i finansowo do wydania. Żyje w warstwie integracyjnej, więc
 * domena Sprzedaży (salon.sales.domain.*) pozostaje nietknięta. eventId służy do deduplikacji.
 */
public record VehicleReadyForHandoverEvent(String eventId, String orderId) {
}
