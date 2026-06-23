package salon.logistics.application.domain.model.vehicle;

public record VinNumber(String value) {

    public VinNumber {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("VIN must not be blank.");
        }
    }

    @Override
    public String toString() {
        return value;
    }
}
