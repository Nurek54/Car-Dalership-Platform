package salon.logistics.infrastructure.mock;

import salon.logistics.application.port.out.ProductionSlotRepository;
import salon.logistics.domain.model.slot.ProductionSlot;
import salon.logistics.domain.model.slot.SlotId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class InMemoryProductionSlotRepository implements ProductionSlotRepository {

    private final Map<SlotId, ProductionSlot> store = new HashMap<>();

    @Override
    public void save(ProductionSlot slot) {
        this.store.put(slot.getId(), slot);
    }

    @Override
    public Optional<ProductionSlot> findById(SlotId id) {
        return Optional.ofNullable(this.store.get(id));
    }

    @Override
    public List<ProductionSlot> findAll() {
        return new ArrayList<>(this.store.values());
    }
}
