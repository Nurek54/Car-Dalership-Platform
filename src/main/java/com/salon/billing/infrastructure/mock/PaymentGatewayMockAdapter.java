package main.java.com.salon.billing.infrastructure.mock;

import main.java.com.salon.billing.application.port.out.PaymentGatewayPort;

/**
 * Adapter do bramki płatniczej — wersja MOCK (tylko loguje potwierdzenie).
 */
public class PaymentGatewayMockAdapter implements PaymentGatewayPort {

    @Override
    public void acknowledgePayment(String gatewayTransactionId) {
        if (gatewayTransactionId == null || gatewayTransactionId.isBlank()) {
            throw new IllegalArgumentException("gatewayTransactionId must not be blank.");
        }
        System.out.println("[PaymentGatewayMockAdapter] Acknowledged transaction: " + gatewayTransactionId);
    }
}
