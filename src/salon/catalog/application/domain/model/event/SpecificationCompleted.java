package salon.catalog.application.domain.model.event;

import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;
import salon.catalog.application.domain.model.specification.SpecificationId;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

/**
 * Domain event emitted after the specification is finalized (UC-KON-01, step 7).
 * Outbound context communication (OHS) – published on the bus (RabbitMQ).
 */
public final class SpecificationCompleted implements DomainEvent {

    private final SpecificationId specificationId;
    private final CatalogId catalogId;
    private final Money totalPrice;
    private final List<OptionCode> optionsPicked;
    private final Instant occurredOn;

    public SpecificationCompleted(SpecificationId specificationId,
                                  CatalogId catalogId,
                                  Money totalPrice,
                                  List<OptionCode> optionsPicked,
                                  Instant occurredOn) {
        this.specificationId = Objects.requireNonNull(specificationId);
        this.catalogId = Objects.requireNonNull(catalogId);
        this.totalPrice = Objects.requireNonNull(totalPrice);
        this.optionsPicked = List.copyOf(optionsPicked);
        this.occurredOn = Objects.requireNonNull(occurredOn);
    }

    public SpecificationId specificationId() {
        return specificationId;
    }

    public CatalogId catalogId() {
        return catalogId;
    }

    public Money totalPrice() {
        return totalPrice;
    }

    public List<OptionCode> optionsPicked() {
        return optionsPicked;
    }

    @Override
    public Instant occurredOn() {
        return occurredOn;
    }

    @Override
    public String eventName() {
        return "SpecificationCompleted";
    }
}
