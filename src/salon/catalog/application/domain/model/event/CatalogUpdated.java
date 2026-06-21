package salon.catalog.application.domain.model.event;

import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.domain.model.catalog.ModelYear;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event emitted after a successful update of the catalog/price list
 * (UC-KON-02, step 5). Published on the data bus (RabbitMQ).
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
