package salon.billing.application.command;

// Komenda dla UC-FIR-01.
// Dane nabywcy (BuyerDetails) NIE są częścią komendy — DocumentAppService dociąga je
// z modułu Sprzedaży/CRM przez CrmIntegrationPort (po orderId).
public record GenerateAdvanceCommand(String orderId,
                                     String authorizedIssuer) {

    public GenerateAdvanceCommand {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        if (authorizedIssuer == null || authorizedIssuer.isBlank()) {
            throw new IllegalArgumentException("authorizedIssuer is required.");
        }
    }
}
