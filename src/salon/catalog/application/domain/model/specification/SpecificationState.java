package salon.catalog.application.domain.model.specification;

public enum SpecificationState {
    DRAFT,         // utworzona, brak wybranych opcji
    IN_PROGRESS,   // w trakcie konfiguracji (wybrano co najmniej jedną opcję)
    FINAL          // skompletowana, gotowa do oferty
}
