package salon.sales.application.handler;

import salon.financing.application.port.out.BankIntegrationAcl;
import salon.sales.application.domain.event.FinancingRequestedEvent;

/**
 * EVENT HANDLER (Figure 22) — reacts to {@link FinancingRequestedEvent} (UC-CRM-03):
 * starts the creditworthiness check in the Financing/Bank context.
 */
public class FinancingRequestedEventHandler {

    private final BankIntegrationAcl financePort;

    public FinancingRequestedEventHandler(BankIntegrationAcl financePort) {
        this.financePort = financePort;
    }

    public void handle(FinancingRequestedEvent event) {
        // We call the Financing module port to start the creditworthiness check process
        this.financePort.startCreditCheckProcess(event.orderId());
    }
}
