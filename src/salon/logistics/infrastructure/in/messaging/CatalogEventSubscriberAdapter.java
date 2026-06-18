package salon.logistics.infrastructure.in.messaging;

import salon.catalog.application.domain.event.SpecificationCompletedEvent;
import salon.logistics.application.port.in.SynchronizeSpecificationUseCase;

/**
 * Adapter sterujący (driving) — subskrybent zdarzeń Kontekstu Katalogu
 * w Kontekście Inwentarza i Logistyki.
 *
 * SpecificationCompleted niesie kody wyposażenia (event-carried state transfer):
 * Inwentarz buduje z nich lokalną kopię specyfikacji, dzięki czemu UC-INW-01
 * (rezerwacja z placu) i UC-INW-02 (zlecenie produkcji) nie wymagają
 * synchronicznego odpytywania Katalogu/Sprzedaży.
 */
public class CatalogEventSubscriberAdapter {

    private final SynchronizeSpecificationUseCase synchronizeSpecification;

    public CatalogEventSubscriberAdapter(SynchronizeSpecificationUseCase synchronizeSpecification) {
        if (synchronizeSpecification == null) {
            throw new IllegalArgumentException("synchronizeSpecification must not be null.");
        }
        this.synchronizeSpecification = synchronizeSpecification;
    }

    public void handleSpecificationCompleted(SpecificationCompletedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        if (event.specificationId() == null || event.specificationId().isBlank()) {
            throw new IllegalArgumentException("Identyfikator specyfikacji jest wymagany");
        }
        this.synchronizeSpecification.registerSpecification(
                event.specificationId(), event.optionCodes());
    }
}
