package salon.billing.application.port.in;

// Komenda dla UC-FIR-02.
public record GenerateInvoiceCommand(String orderId,
                                     String buyerName,
                                     String buyerNip,
                                     String invoiceTitle,
                                     String authorizedIssuer) {

    public GenerateInvoiceCommand {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        if (buyerName == null || buyerName.isBlank()) {
            throw new IllegalArgumentException("buyerName is required.");
        }
        if (invoiceTitle == null || invoiceTitle.isBlank()) {
            throw new IllegalArgumentException("invoiceTitle is required.");
        }
        if (authorizedIssuer == null || authorizedIssuer.isBlank()) {
            throw new IllegalArgumentException("authorizedIssuer is required.");
        }
        // buyerNip może być nullem (osoba fizyczna).
    }
}
