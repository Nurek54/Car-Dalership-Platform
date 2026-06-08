package salon.billing.application.port.in;

// Komenda dla UC-FIR-01.
public record GenerateAdvanceCommand(String orderId,
                                     String buyerName,
                                     String buyerNip,
                                     String authorizedIssuer) {

    public GenerateAdvanceCommand {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        if (buyerName == null || buyerName.isBlank()) {
            throw new IllegalArgumentException("buyerName is required.");
        }
        if (authorizedIssuer == null || authorizedIssuer.isBlank()) {
            throw new IllegalArgumentException("authorizedIssuer is required.");
        }
        // buyerNip może być nullem (osoba fizyczna).
    }
}
