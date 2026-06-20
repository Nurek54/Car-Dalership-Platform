package salon.catalog.application.domain.model.specification;

/**
 * Typ standardowy (enumeracja) – stan procesu konfiguracji pojazdu.
 *  - DRAFT       – wersja robocza (np. po przerwaniu sesji – UC-KON-01 / A2),
 *  - IN_PROGRESS – trwa dobieranie opcji,
 *  - FINAL        – specyfikacja zatwierdzona (po finalizeSpecification()).
 */
public enum SpecificationState {
    DRAFT,
    IN_PROGRESS,
    FINAL
}
