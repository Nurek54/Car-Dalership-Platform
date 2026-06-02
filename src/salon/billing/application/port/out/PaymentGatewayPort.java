package salon.billing.application.port.out;

// Port wyjściowy do bramki płatniczej.
public interface PaymentGatewayPort {
    void acknowledgePayment(String gatewayTransactionId);
}
