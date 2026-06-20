package salon.billing.application.domain.model.document;

/**
 * OBIEKT WARTOŚCI (Diagram klas — «ValueObject» BuyerDetails): dane nabywcy na fakturze.
 *
 * Niemutowalny, modeluje pojęciową całość (nazwa + NIP). Wypełniany przez ACL na podstawie
 * migawki z Kontekstu Sprzedaży (CustomerSnapshotDto). Operacja {@link #isCorporate()} jest
 * zapytaniem bez efektów ubocznych — decyduje o terminie płatności faktury (UC-FIR-01/02).
 */
public record BuyerDetails(String name, String nip) {

    public BuyerDetails {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("name must not be blank.");
        }
        if (nip == null || nip.isBlank()) {
            throw new IllegalArgumentException("nip must not be blank.");
        }
    }

    /** Nabywca firmowy = poprawny 10-cyfrowy NIP (dłuższy termin płatności niż dla osoby fizycznej). */
    public boolean isCorporate() {
        String digits = this.nip.replaceAll("\\D", "");
        return digits.length() == 10;
    }
}
