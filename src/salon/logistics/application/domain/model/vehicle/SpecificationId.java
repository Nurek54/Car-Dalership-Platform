package salon.logistics.application.domain.model.vehicle;

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
