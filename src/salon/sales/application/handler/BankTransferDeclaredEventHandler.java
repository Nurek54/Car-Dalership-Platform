package salon.sales.application.handler;

import salon.sales.application.domain.event.BankTransferDeclaredEvent;
import salon.sales.application.port.out.BillingIntegration;

/**
 * EVENT HANDLER (Figure 22) — reacts to {@link BankTransferDeclaredEvent} (UC-CRM-03):
 * asks Billing to issue a proforma invoice for the declared amount.
 */
public class BankTransferDeclaredEventHandler {

    private final BillingIntegration billingPort;

    public BankTransferDeclaredEventHandler(BillingIntegration billingPort) {
        this.billingPort = billingPort;
    }

    public void handle(BankTransferDeclaredEvent event) {
        // We instruct the Billing department to issue a proforma document
        this.billingPort.requestProformaInvoice(event.orderId(), event.amount());
    }
}
