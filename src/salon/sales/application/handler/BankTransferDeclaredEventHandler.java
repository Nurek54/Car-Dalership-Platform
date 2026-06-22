package salon.sales.application.handler;

import salon.sales.application.domain.event.BankTransferDeclaredEvent;
import salon.sales.application.port.out.BillingIntegration;

public class BankTransferDeclaredEventHandler {

    private final BillingIntegration billingPort;

    public BankTransferDeclaredEventHandler(BillingIntegration billingPort) {
        this.billingPort = billingPort;
    }

    public void handle(BankTransferDeclaredEvent event) {
        
        this.billingPort.requestProformaInvoice(event.orderId(), event.amount());
    }
}
