package salon.billing.application.port.in;

// Port wejściowy (driving) dla UC-FIR-03: rejestracja wpłaty z wyciągu bankowego.
public interface ProcessPaymentUseCase {
    void processPayment(ProcessPaymentCommand command);
}
