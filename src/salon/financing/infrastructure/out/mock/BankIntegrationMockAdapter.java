package salon.financing.infrastructure.out.mock;

import salon.financing.application.port.out.BankIntegrationAcl;

public class BankIntegrationMockAdapter implements BankIntegrationAcl {

    @Override
    public void submitFinancingApplication(String orderId) {
        System.out.println("[BankIntegrationMockAdapter] Financing application for order "
                + orderId + " sent to the bank — awaiting the asynchronous decision.");
    }
}
