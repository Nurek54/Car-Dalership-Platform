package salon.billing.infrastructure.mock;

import salon.billing.application.port.out.PaymentGatewayPort;

public class PaymentGatewayMockAdapter implements PaymentGatewayPort {

    @Override
    public void acknowledgePayment(String gatewayTransactionId) {
        System.out.println("[PaymentGatewayMock] Acknowledged transaction: " + gatewayTransactionId);
    }
}
