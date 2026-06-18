package salon.catalog.application.domain.model.catalog;

// Value Object: kod opcji wyposażenia (np. "LED_LIGHTS", "MANUAL_GEARBOX").
public record OptionCode(String value) {

    public OptionCode {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("OptionCode must not be blank.");
        }
    }
}
