package salon.billing.application.port.in;

import salon.billing.application.command.GenerateInvoiceCommand;

// Port wejściowy dla UC-FIR-02: wystawienie faktury końcowej. Zwraca id dokumentu.
public interface GenerateInvoiceUseCase {
    String generateInvoice(GenerateInvoiceCommand command);
}
