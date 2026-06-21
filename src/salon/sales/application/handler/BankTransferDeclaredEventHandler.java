package salon.sales.application.handler;

import salon.sales.application.port.out.BillingIntegration;
import salon.sales.application.domain.event.BankTransferDeclaredEvent;

/**
 * Handler of the BankTransferDeclared event (UC-CRM-03, step 5 — transfer):
 * it instructs the Billing Context to issue a proforma document with transfer details.
 */
public class BankTransferDeclaredEventHandler {

    private final BillingIntegration billingPort;

    public BankTransferDeclaredEventHandler(BillingIntegration billingPort) {
        this.billingPort = billingPort;
    }

    public void handle(BankTransferDeclaredEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        this.billingPort.requestProformaInvoice(event.orderId(), event.declaredAmount());
    }
}
