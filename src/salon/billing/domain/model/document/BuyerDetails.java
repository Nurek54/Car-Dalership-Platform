package salon.billing.domain.model.document;

/**
 * Value Object: dane nabywcy. Niemutowalny — chroni przed antywzorcem God Class
 * (szczegóły dokumentu pogrupowane w logiczne obiekty wartości).
 *
 * NIP bywa pusty (np. paragon / osoba fizyczna) — dlatego go nie blokujemy. Obecność NIP
 * decyduje o tym, czy nabywca jest podmiotem gospodarczym (isCorporate), co wpływa na termin płatności.
 */
public record BuyerDetails(String name, String nip) {

    public BuyerDetails {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Buyer name is required.");
        }
    }

    public boolean isCorporate() {
        return this.nip != null && !this.nip.isBlank();
    }
}
