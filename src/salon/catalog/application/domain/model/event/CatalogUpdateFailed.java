package salon.catalog.application.domain.model.event;

import java.time.Instant;
import java.util.Objects;

public final class CatalogUpdateFailed implements DomainEvent {

    private final String reason;
    private final Instant occurredOn;

    public CatalogUpdateFailed(String reason, Instant occurredOn) {
        this.reason = Objects.requireNonNull(reason);
        this.occurredOn = Objects.requireNonNull(occurredOn);
    }

    public String reason() {
        return reason;
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    @Override
    public String eventName() {
        return "CatalogUpdateFailed";
    }
}
