package salon.sales.application.port.in;

// Port wejściowy dla UC-SPR-03.
public interface CancelOrderUseCase {
    void cancelOrder(CancelOrderCommand command);
}
