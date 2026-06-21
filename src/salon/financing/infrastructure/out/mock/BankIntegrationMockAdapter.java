package salon.financing.infrastructure.out.mock;

import salon.financing.application.port.out.BankIntegrationAcl;

/**
 * OUTBOUND ADAPTER (ACL, Figure 42: BankService) – a mock integration with the bank system.
 *
 * Simulates asynchronous application submission: the bank is the sole decision-maker, and the decision returns
 * later as a separate event (UC-FIN-02). A variant throwing an exception would correspond to A2
 * (an immediate validation rejection on the bank side).
 */
public class BankIntegrationMockAdapter implements BankIntegrationAcl {

    @Override
    public void submitFinancingApplication(String orderId) {
        System.out.println("[BankIntegrationMockAdapter] Financing application for order "
                + orderId + " sent to the bank — awaiting the asynchronous decision.");
    }
}
