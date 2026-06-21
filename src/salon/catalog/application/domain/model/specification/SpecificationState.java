package salon.catalog.application.domain.model.specification;

/**
 * Standard type (enumeration) – the state of the vehicle configuration process.
 *  - DRAFT       – wersja robocza (np. po przerwaniu sesji – UC-KON-01 / A2),
 *  - IN_PROGRESS – options are being selected,
 *  - FINAL        – the specification is finalized (after finalizeSpecification()).
 */
public enum SpecificationState {
    DRAFT,
    IN_PROGRESS,
    FINAL
}
