package salon.billing.application.port.in;

import salon.billing.application.command.GenerateInvoiceCommand;

/**
 * PORT WEJSCIOWY (Rys. 48 — GenerateInvoice) — UC-FIR-02: Stworzenie faktury koncowej.
 *
 * Wyzwalany zdarzeniem VehicleReservedFromStock. Wystawia fakture na kwote pozostala do zaplaty,
 * generuje PDF, powiadamia klienta i emituje InvoiceCreated.
 */
public interface GenerateInvoice {

    /** @return identyfikator wystawionej faktury. */
    String generateInvoice(GenerateInvoiceCommand command);
}
