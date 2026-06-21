package salon.catalog.application.domain.model.catalog;

/**
 * Value object – the model year for which the catalog/price list applies.
 */
public final class ModelYear {

    private final int year;

    private ModelYear(int year) {
        if (year < 1900 || year > 2200) {
            throw new IllegalArgumentException("Niepoprawny rocznik modelowy: " + year);
        }
        this.year = year;
    }

    public static ModelYear of(int year) {
        return new ModelYear(year);
    }

    public int year() {
        return year;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ModelYear modelYear)) return false;
        return year == modelYear.year;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(year);
    }

    @Override
    public String toString() {
        return String.valueOf(year);
    }
}
