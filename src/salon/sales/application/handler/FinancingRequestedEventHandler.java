package salon.sales.application.handler;

import salon.financing.application.port.out.BankIntegrationAcl;
import salon.sales.application.domain.event.FinancingRequestedEvent;

/**
 * Handler zdarzenia FinancingRequested (UC-CRM-03, krok 5 — finansowanie):
 * uruchamia w module Finansowym proces sprawdzania zdolności kredytowej/leasingowej
 * (UC-FIN-01, przez ACL banku).
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
