package salon.financing.application.domain.model.financing;

public record CustomerId(String value) {

    public CustomerId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("CustomerId must not be blank.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
