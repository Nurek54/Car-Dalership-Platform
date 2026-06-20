package salon.catalog.application.domain.model.event;

import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.domain.model.catalog.ModelYear;

import java.time.Instant;
import java.util.Objects;

/**
 * Zdarzenie dziedziny emitowane po pomyślnej aktualizacji katalogu/cennika
 * (UC-KON-02, krok 5). Publikowane na szynę danych (RabbitMQ).
 */
public final class CatalogUpdated implements DomainEvent {

    private final CatalogId catalogId;
    private final ModelYear modelYear;
    private final int version;
    private final Instant occurredOn;

    public CatalogUpdated(CatalogId catalogId, ModelYear modelYear, int version, Instant occurredOn) {
        this.catalogId = Objects.requireNonNull(catalogId);
        this.modelYear = Objects.requireNonNull(modelYear);
        this.version = version;
        this.occurredOn = Objects.requireNonNull(occurredOn);
    }

    public CatalogId catalogId() {
        return catalogId;
    }

    public ModelYear modelYear() {
        return modelYear;
    }

    public int version() {
        return version;
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    @Override
    public String eventName() {
        return "CatalogUpdated";
    }
}
