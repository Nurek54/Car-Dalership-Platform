package salon.logistics.application.domain.model.vehicle;

/**
 * Value object: the VIN — the natural, immutable identifier of a physical vehicle.
 * Per the canvas assumption the VIN scan is error-free and matches the digital data
 * of the production order.
 */
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
