package salon.sales.application.handler;

import salon.financing.application.port.out.BankIntegrationAcl;
import salon.sales.application.domain.event.FinancingRequestedEvent;

public class FinancingRequestedEventHandler {

    private final BankIntegrationAcl financePort;

    public FinancingRequestedEventHandler(BankIntegrationAcl financePort) {
        this.financePort = financePort;
    }

    public void handle(FinancingRequestedEvent event) {
        
        this.financePort.startCreditCheckProcess(event.orderId());
    }
}
