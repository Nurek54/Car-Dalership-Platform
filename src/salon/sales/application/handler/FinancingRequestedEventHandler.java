package salon.sales.application.handler;

import salon.financing.application.port.out.BankIntegrationAclPort;
import salon.sales.domain.event.FinancingRequestedEvent;

/**
 * Handler zdarzenia FinancingRequested (UC-CRM-03, krok 5 — finansowanie):
 * uruchamia w module Finansowym proces sprawdzania zdolności kredytowej/leasingowej
 * (UC-FIN-01, przez ACL banku).
 */
public class FinancingRequestedEventHandler {

    private final BankIntegrationAclPort financePort;

    public FinancingRequestedEventHandler(BankIntegrationAclPort financePort) {
        this.financePort = financePort;
    }

    public void handle(FinancingRequestedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        this.financePort.startCreditCheckProcess(event.orderId());
    }
}
