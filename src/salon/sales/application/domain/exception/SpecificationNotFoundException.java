package salon.sales.application.domain.exception;

/** Katalog nie zna wskazanej specyfikacji (UC-CRM-02 — wycena niemożliwa). */
public class SpecificationNotFoundException extends RuntimeException {
    public SpecificationNotFoundException(String message) {
        super(message);
    }
}
