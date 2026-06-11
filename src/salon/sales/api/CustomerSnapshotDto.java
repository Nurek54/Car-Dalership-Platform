package salon.sales.api;

/**
 * Published Language Kontekstu Sprzedaży i CRM — migawka danych nabywcy
 * udostępniana innym kontekstom (np. Fakturowaniu) bez ujawniania agregatów
 * domenowych ({@code Customer}, {@code Offer}, {@code Order}).
 *
 * Zawiera wyłącznie dane potrzebne konsumentom; konteksty-klienci tłumaczą
 * ten DTO na własne obiekty wartości w swoich ACL.
 */
public record CustomerSnapshotDto(String fullName, String nip) {

    public CustomerSnapshotDto {
        if (fullName == null || fullName.isBlank()) {
            throw new IllegalArgumentException("fullName must not be null or blank.");
        }
        if (nip == null || nip.isBlank()) {
            throw new IllegalArgumentException("nip must not be null or blank.");
        }
    }
}
