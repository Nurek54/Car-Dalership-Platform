package salon.sales.domain.model.order;

public enum CancellationReason {
    CLIENT_FAULT,  // wina klienta (np. rezygnacja) -> zwykle zatrzymujemy zadatek
    DEALER_FAULT,  // wina salonu -> zwrot zadatku
    NONE           // brak (zamówienie nieanulowane)
}
