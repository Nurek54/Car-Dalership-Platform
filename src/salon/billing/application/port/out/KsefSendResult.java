package salon.billing.application.port.out;

// Wynik wysyłki do KSeF. accepted = false oznacza brak odpowiedzi z KSeF (UC-ROZ-02, A1).
public record KsefSendResult(boolean accepted, String ksefReference) {
}
