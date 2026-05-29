package main.java.com.salon.billing.application.port.in;

// Port wejściowy (driving port) dla UC-ROZ-01.
public interface RegisterPaymentUseCase {
    void registerPayment(RegisterPaymentCommand command);
}