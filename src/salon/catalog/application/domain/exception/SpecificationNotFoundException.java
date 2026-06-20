package salon.catalog.application.domain.exception;

import salon.catalog.application.domain.model.specification.SpecificationId;

public class SpecificationNotFoundException extends RuntimeException {

    public SpecificationNotFoundException(SpecificationId id) {
        super("Nie znaleziono specyfikacji o id: " + id);
    }
}
