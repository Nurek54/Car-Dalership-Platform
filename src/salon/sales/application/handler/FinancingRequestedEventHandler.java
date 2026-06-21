package salon.sales.application.handler;

import salon.financing.application.port.out.BankIntegrationAcl;
import salon.sales.application.domain.event.FinancingRequestedEvent;

/**
 * Handler of the FinancingRequested event (UC-CRM-03, step 5 — financing):
 * it triggers the creditworthiness/leasing-capacity check process in the Financing module
 * (UC-FIN-01, through the bank ACL).
 */
public class FinancingRequestedEventHandler {

    private final BankIntegrationAcl financePort;

    public FinancingRequestedEventHandler(BankIntegrationAcl financePort) {
        this.financePort = financePort;
    }

    public void handle(FinancingRequestedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        this.financePort.startCreditCheckProcess(event.orderId());
    }
}
