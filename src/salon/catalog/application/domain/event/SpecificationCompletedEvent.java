package salon.catalog.application.domain.event;

import salon.common.event.DomainEvent;
import salon.common.model.Money;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

// "SpecyfikacjaSkompletowana" (UC-KON-01) - konfiguracja gotowa do sprzedazy;
// to zdarzenie odblokowuje przygotowanie oferty w Kontekscie Sprzedazy.
//
// Zdarzenie niesie pelna liste kodow wyposazenia ORAZ wyliczona cene katalogowa
// (event-carried state transfer): dzieki temu konteksty downstream (Sprzedaz,
// Inwentarz) buduja lokalne kopie specyfikacji i wyceny, i nie musza synchronicznie
// odpytywac Katalogu (PDF: UC-CRM-02 - specyfikacja i cena katalogowa uzupelniane
// z otrzymanego zdarzenia; UC-INW-01 - specyfikacja z zamowienia).
//
// Uwaga (granica kontekstu): zdarzenie NIE niesie danych klienta - to jezyk
// Kontekstu Sprzedazy, ktorego Katalog nie jest wlascicielem. Dane klienta podaje
// Handlowiec przy generowaniu oferty (UC-CRM-02, scenariusz glowny, krok 3).
public record SpecificationCompletedEvent(UUID eventId,
                                          String specificationId,
                                          String catalogId,
                                          Money price,
                                          List<String> optionCodes,
                                          Instant occurredOn) implements DomainEvent {

    public SpecificationCompletedEvent {
        optionCodes = optionCodes == null ? List.of() : List.copyOf(optionCodes);
    }
}
