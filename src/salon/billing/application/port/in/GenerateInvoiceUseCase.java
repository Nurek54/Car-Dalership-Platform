package salon.billing.application.port.in;

// Port wejściowy dla UC-FIR-02: wystawienie faktury końcowej. Zwraca id dokumentu.
public interface GenerateInvoiceUseCase {
    String generateInvoice(GenerateInvoiceCommand command);
}
