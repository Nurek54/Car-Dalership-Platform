package salon.catalog.application.domain.model.specification;

import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;

import java.util.Set;

/**
 * Fabryka agregatu VehicleSpecification.
 *
 * Tworzy specyfikację w spójnym stanie początkowym: nowy globalny identyfikator,
 * stan DRAFT, zerowa cena całkowita i pusty zbiór opcji. Atomowa – nigdy nie zwraca
 * obiektu niepoprawnego.
 */
public class VehicleSpecificationFactory {

    /**
     * Otwarcie sesji konfiguratora (UC-KON-01, krok 1) – tworzy roboczą specyfikację
     * powiązaną z aktywnym katalogiem przez {@link CatalogId}.
     */
    public VehicleSpecification createDraft(CatalogId catalogId, String currencyCode) {
        return new VehicleSpecification(
                SpecificationId.generate(),
                catalogId,
                Money.zero(currencyCode),
                SpecificationState.DRAFT,
                Set.of());
    }

    /** Odtworzenie agregatu z trwałego magazynu (używane przez adapter repozytorium). */
    public VehicleSpecification reconstitute(SpecificationId id,
                                             CatalogId catalogId,
                                             Money totalPrice,
                                             SpecificationState state,
                                             Set<OptionCode> optionsPicked) {
        return new VehicleSpecification(id, catalogId, totalPrice, state, optionsPicked);
    }
}
