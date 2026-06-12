package salon.catalog.domain.event;

import salon.shared.event.DomainEvent;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// "SpecyfikacjaSkompletowana" (UC-KON-01) — konfiguracja gotowa do sprzedaży;
// to zdarzenie odblokowuje przygotowanie oferty w Kontekście Sprzedaży.
//
// Zdarzenie niesie pełną listę kodów wyposażenia (event-carried state transfer):
// dzięki temu konteksty downstream (Sprzedaż, Inwentarz) budują lokalne kopie
// specyfikacji i nie muszą synchronicznie odpytywać Katalogu (PDF: UC-CRM-02
// "dane otrzymane od zdarzenia", UC-INW-01 "specyfikacja z zamówienia").
public record SpecificationCompletedEvent(UUID eventId,
                                          String specificationId,
                                          String catalogId,
                                          List<String> optionCodes,
                                          Instant occurredOn) implements DomainEvent {

    public SpecificationCompletedEvent {
        optionCodes = optionCodes == null ? List.of() : List.copyOf(optionCodes);
    }
}
