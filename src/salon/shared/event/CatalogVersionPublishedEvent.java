package salon.shared.event;

/**
 * Kontrakt zdarzenia integracyjnego "CatalogVersionPublished" (UC-KON-02) przechodzącego przez kolejkę.
 *
 * Świadomie NIE implementuje {@link DomainEvent}: identyfikatorem niesionym przez magistralę jest
 * tu String (np. "EVT-5001"), a nie UUID. Subskrybent w Sprzedaży reaguje, unieważniając oferty
 * oparte o starsze cenniki. To dodatkowy plik w shared kernel — nie zmienia istniejących kontekstów.
 */
public record CatalogVersionPublishedEvent(String eventId, String catalogId, String modelYear) {
}
