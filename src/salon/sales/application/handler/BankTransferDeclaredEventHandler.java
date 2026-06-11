package salon.sales.application.handler;

import salon.sales.application.port.out.BillingIntegrationPort;
import salon.sales.domain.event.BankTransferDeclaredEvent;

/**
 * Handler zdarzenia BankTransferDeclared (UC-CRM-03, krok 5 — przelew):
 * zleca Kontekstowi Rozliczeń wystawienie dokumentu proforma z danymi do przelewu.
 */
public class BankTransferDeclaredEventHandler {

    private final BillingIntegrationPort billingPort;

    public BankTransferDeclaredEventHandler(BillingIntegrationPort billingPort) {
        this.billingPort = billingPort;
    }

    public void handle(BankTransferDeclaredEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        this.billingPort.requestProformaInvoice(event.orderId(), event.declaredAmount());
    }
}
