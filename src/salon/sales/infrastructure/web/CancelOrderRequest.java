package salon.sales.infrastructure.web;

/** DTO wejściowe anulowania zamówienia: powód rezygnacji klienta. */
public record CancelOrderRequest(String reason) {
}
