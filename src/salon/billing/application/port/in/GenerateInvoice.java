package salon.billing.application.port.in;

import salon.billing.application.command.GenerateInvoiceCommand;

public interface GenerateInvoice {

    String generateInvoice(GenerateInvoiceCommand command);
}
