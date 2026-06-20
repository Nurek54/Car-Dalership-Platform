package salon.billing.application.command;

/**
 * Model danych wejsciowych portu GenerateInvoice (UC-FIR-02).
 */
public record GenerateInvoiceCommand(String orderId, String invoiceTitle, String authorizedIssuer) {

    public GenerateInvoiceCommand {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        if (invoiceTitle == null || invoiceTitle.isBlank()) {
            throw new IllegalArgumentException("invoiceTitle must not be blank.");
        }
        if (authorizedIssuer == null || authorizedIssuer.isBlank()) {
            throw new IllegalArgumentException("authorizedIssuer must not be blank.");
        }
    }
}
