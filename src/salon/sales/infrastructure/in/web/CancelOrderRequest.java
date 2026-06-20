package salon.sales.infrastructure.in.web;

/** DTO wejściowe anulowania zamówienia: powód rezygnacji klienta. */
public record CancelOrderRequest(String reason) {
}
