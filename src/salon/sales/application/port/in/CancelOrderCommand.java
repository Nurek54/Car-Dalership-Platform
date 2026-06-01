package salon.sales.application.port.in;

import salon.sales.domain.model.order.CancellationReason;

public record CancelOrderCommand(String orderId,
                                 CancellationReason reason,
                                 boolean isHandedOver) {

    public CancelOrderCommand {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        if (reason == null) {
            throw new IllegalArgumentException("reason must not be null.");
        }
    }
}
