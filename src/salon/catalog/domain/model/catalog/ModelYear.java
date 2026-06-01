package salon.catalog.domain.model.catalog;

// Value Object: rocznik/wersja cennika (np. "MY_2026").
public record ModelYear(String value) {

    public ModelYear {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ModelYear must not be blank.");
        }
    }
}
