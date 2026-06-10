package salon.logistics.application;

import salon.logistics.application.port.in.TrackProductionUseCase;
import salon.logistics.application.port.out.FactoryStatusAclPort;
import salon.logistics.application.port.out.ProductionSlotRepository;
import salon.logistics.domain.model.slot.FactoryStatus;
import salon.logistics.domain.model.slot.ProductionSlot;
import salon.logistics.domain.model.slot.SlotId;
import salon.shared.application.EventPublisherPort;
import salon.shared.event.DomainEvent;

import java.time.LocalDate;
import java.util.List;

/**
 * Usługa aplikacyjna śledzenia produkcji —
 * węzeł "ProductionTrackingAppService" w docs/Inwentarz-Logistyka/LogisticsArchitecture.md.
 */
public class ProductionTrackingAppService implements TrackProductionUseCase {

    private final ProductionSlotRepository slotRepository;
    private final FactoryStatusAclPort factoryStatus;
    private final EventPublisherPort eventPublisher;

    public ProductionTrackingAppService(ProductionSlotRepository slotRepository,
                                        FactoryStatusAclPort factoryStatus,
                                        EventPublisherPort eventPublisher) {
        if (slotRepository == null) {
            throw new IllegalArgumentException("slotRepository must not be null.");
        }
        if (factoryStatus == null) {
            throw new IllegalArgumentException("factoryStatus must not be null.");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        this.slotRepository = slotRepository;
        this.factoryStatus = factoryStatus;
        this.eventPublisher = eventPublisher;
    }

    // Odpytuje fabrykę o status slotu i aktualizuje agregat (emisja DeliveryEtaUpdatedEvent przy zmianie ETA).
    @Override
    public void updateProductionStatus(String slotId, LocalDate estimatedDelivery) {
        if (slotId == null || slotId.isBlank()) {
            throw new IllegalArgumentException("slotId must not be blank.");
        }
        ProductionSlot slot = slotRepository.findById(new SlotId(slotId))
                .orElseThrow(() -> new IllegalStateException("No production slot " + slotId));

        FactoryStatus status = factoryStatus.fetchStatus(slot.getFactoryJobId());
        slot.updateFactoryStatus(status, estimatedDelivery);
        slotRepository.save(slot);

        List<DomainEvent> events = slot.pullDomainEvents();
        for (int i = 0; i < events.size(); i++) {
            eventPublisher.publish(events.get(i));
        }
    }
}
