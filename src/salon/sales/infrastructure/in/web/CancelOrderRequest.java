package salon.sales.infrastructure.in.web;

/** Input DTO for order cancellation: the customer's cancellation reason. */
public record CancelOrderRequest(String reason) {
}
