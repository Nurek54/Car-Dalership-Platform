package salon.logistics.infrastructure.in.messaging;

import salon.catalog.application.domain.model.event.SpecificationCompleted;
import salon.catalog.application.domain.model.shared.OptionCode;
import salon.logistics.application.port.in.SynchronizeSpecificationUseCase;

import java.util.List;

/**
 * Adapter sterujący (driving) — subskrybent zdarzeń Kontekstu Katalogu
 * w Kontekście Inwentarza i Logistyki.
 *
 * SpecificationCompleted niesie kody wyposażenia (event-carried state transfer):
 * Inwentarz buduje z nich lokalną kopię specyfikacji, dzięki czemu UC-INW-01
 * (rezerwacja z placu) i UC-INW-02 (zlecenie produkcji) nie wymagają
 * synchronicznego odpytywania Katalogu/Sprzedaży.
 *
 * Warstwa zapobiegajaca uszkodzeniu (ACL): adapter tlumaczy typy Katalogu
 * (SpecificationId, OptionCode) na prosty model portu Logistyki (String, List<String>).
 */
public class CatalogEventSubscriberAdapter {

    private final SynchronizeSpecificationUseCase synchronizeSpecification;

    public CatalogEventSubscriberAdapter(SynchronizeSpecificationUseCase synchronizeSpecification) {
        if (synchronizeSpecification == null) {
            throw new IllegalArgumentException("synchronizeSpecification must not be null.");
        }
        this.synchronizeSpecification = synchronizeSpecification;
    }

    public void handleSpecificationCompleted(SpecificationCompleted event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        // Tlumaczenie z jezyka Katalogu na model Logistyki (ACL).
        String specificationId = event.specificationId().toString();
        List<String> optionCodes = event.optionsPicked().stream()
                .map(OptionCode::value)
                .toList();

        this.synchronizeSpecification.registerSpecification(specificationId, optionCodes);
    }
}
