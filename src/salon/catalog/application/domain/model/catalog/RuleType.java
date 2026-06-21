package salon.catalog.application.domain.model.catalog;

/**
 * Standard type (enumeration) – the kind of dependency rule between options.
 *  - REQUIRES – selecting the source option requires the presence of the target option,
 *  - EXCLUDES – selecting the source option excludes the target option
 *               (e.g. engine B2 cannot be selected with gearbox C1 – UC-KON-01 / A1).
 */
public enum RuleType {
    REQUIRES,
    EXCLUDES
}
