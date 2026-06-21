package salon.logistics.application.domain.model.vehicle;

/**
 * Value object: a disjoint reference to a vehicle specification from the Catalog Context.
 * Inventory does not know the Catalog model — it stores only the identifier and (locally)
 * the equipment codes delivered by an event (event-carried state transfer).
 */
public record SpecificationId(String value) {

    public SpecificationId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("SpecificationId must not be blank.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
