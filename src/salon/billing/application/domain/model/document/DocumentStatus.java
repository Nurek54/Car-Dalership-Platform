package salon.billing.application.domain.model.document;

/**
 * Typ standardowy (Enumeration na diagramie klas) — cykl życia dokumentu księgowego.
 *
 * DRAFT  – utworzony lokalnie, przed wystawieniem;
 * ISSUED – wystawiony (PDF wygenerowany i przypisany do zamówienia) — UC-FIR-02;
 * ERROR  – błąd generowania dokumentu (UC-FIR-02 / A1).
 */
public enum DocumentStatus {
    DRAFT,
    ISSUED,
    ERROR
}
