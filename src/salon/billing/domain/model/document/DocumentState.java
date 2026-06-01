package salon.billing.domain.model.document;

public enum DocumentState {
    DRAFT,        // wersja robocza
    PENDING_KSEF, // "Oczekuje na wysyłkę KSeF" (UC-ROZ-02, A1)
    ISSUED,       // wystawiona i zarejestrowana w KSeF
    FAILED        // błąd przetwarzania
}
