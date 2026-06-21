package salon.billing.application.domain.model.document;

/**
 * Standard type (Enumeration in the class diagram) — life cycle of the accounting document.
 *
 * DRAFT  – created locally, before issuing;
 * ISSUED – issued (PDF generated and assigned to the order) — UC-FIR-02;
 * ERROR  – document generation error (UC-FIR-02 / A1).
 */
public enum DocumentStatus {
    DRAFT,
    ISSUED,
    ERROR
}
