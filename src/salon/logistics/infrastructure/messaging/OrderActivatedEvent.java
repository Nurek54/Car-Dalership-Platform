package salon.logistics.infrastructure.messaging;

import java.util.List;

/**
 * Kontrakt integracyjny zdarzenia "ZamówienieAktywowane/Złożone" wpadającego z kolejki do
 * Kontekstu Inwentarza. Niesie kody specyfikacji (specCodes) potrzebne do alokacji pojazdu
 * (UC-INW-02 / production-slot.md). Żyje w warstwie integracyjnej, więc Kontekst Sprzedaży
 * (salon.sales.*) pozostaje nietknięty.
 */
public record OrderActivatedEvent(String eventId, String orderId, List<String> specCodes) {
}
