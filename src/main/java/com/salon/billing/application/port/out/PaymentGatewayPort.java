package main.java.com.salon.billing.application.port.out;

// Port wyjściowy do bramki płatniczej.
public interface PaymentGatewayPort {
    // Potwierdzenie do bramki, że wpłatę przetworzyliśmy po naszej stronie.
    void acknowledgePayment(String gatewayTransactionId);
}