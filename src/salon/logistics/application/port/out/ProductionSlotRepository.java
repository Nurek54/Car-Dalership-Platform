package salon.logistics.application.port.out;

import salon.logistics.domain.model.slot.ProductionSlot;
import salon.logistics.domain.model.slot.SlotId;

import java.util.List;
import java.util.Optional;

public interface ProductionSlotRepository {
    void save(ProductionSlot slot);
    Optional<ProductionSlot> findById(SlotId id);
    List<ProductionSlot> findAll();
}
