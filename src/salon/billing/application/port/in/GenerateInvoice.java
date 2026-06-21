package salon.billing.application.port.in;

import salon.billing.application.command.GenerateInvoiceCommand;

/**
 * INBOUND PORT (Fig. 48 — GenerateInvoice) — UC-FIR-02: Creating the final invoice.
 *
 * Triggered by the VehicleReservedFromStock event. Issues an invoice for the amount remaining due,
 * generates the PDF, notifies the customer and emits InvoiceCreated.
 */
public interface GenerateInvoice {

    /** @return identifier of the issued invoice. */
    String generateInvoice(GenerateInvoiceCommand command);
}
