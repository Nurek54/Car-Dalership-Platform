package salon.billing.application.command;

// Komenda dla UC-FIR-02.
// Dane nabywcy (BuyerDetails) NIE są częścią komendy — zdarzenie wyzwalające
// (VehicleReservedFromStock) niesie tylko orderId/VIN, więc DocumentGenerationService
// dociąga dane klienta z modułu Sprzedaży/CRM przez SalesIntegration.
public record GenerateInvoiceCommand(String orderId,
                                     String invoiceTitle,
                                     String authorizedIssuer) {

    public GenerateInvoiceCommand {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        if (invoiceTitle == null || invoiceTitle.isBlank()) {
            throw new IllegalArgumentException("invoiceTitle is required.");
        }
        if (authorizedIssuer == null || authorizedIssuer.isBlank()) {
            throw new IllegalArgumentException("authorizedIssuer is required.");
        }
    }
}
