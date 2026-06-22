package salon.financing.application.domain.model.financing;

public record OrderId(String value) {

    public OrderId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OrderId must not be blank.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
