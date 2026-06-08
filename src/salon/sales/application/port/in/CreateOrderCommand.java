package salon.sales.application.port.in;

/**
 * Komenda dla UC-SPR-02 (konwersja Oferty w Zamowienie).
 * offerId — oferta zrodlowa; customerSignature — referencja e-podpisu klienta.
 */
public record CreateOrderCommand(String offerId, String customerSignature) {

    public CreateOrderCommand {
        if (offerId == null || offerId.isBlank()) {
            throw new IllegalArgumentException("offerId must not be blank.");
        }
        if (customerSignature == null || customerSignature.isBlank()) {
            throw new IllegalArgumentException("customerSignature must not be blank.");
        }
    }
}
