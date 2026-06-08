package salon.logistics.domain.model.slot;

import salon.logistics.domain.event.DeliveryEtaUpdatedEvent;
import salon.shared.event.AbstractAggregateRoot;
import salon.shared.model.OrderId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Aggregate Root: slot produkcyjny (ścieżka Long Track, UC-INW-04/05).
 * Lekki agregat istniejący do momentu fizycznego dostarczenia auta na plac.
 *
 * ZALOZENIE: specCodes nie są przechowywane w polach (diagram ich nie ma) — służą tylko
 * do złożenia zamówienia produkcyjnego; walidujemy ich obecność.
 */
public class ProductionSlot extends AbstractAggregateRoot {

    private final SlotId id;
    private final OrderId orderId;
    private String factoryJobId;          // nadawany po rejestracji w fabryce
    private LocalDate estimatedDelivery;  // estymowana data dostawy
    private SlotState state;

    private ProductionSlot(SlotId id, OrderId orderId) {
        this.id = id;
        this.orderId = orderId;
        this.factoryJobId = null;
        this.estimatedDelivery = null;
        this.state = SlotState.SCHEDULED;
    }

    // Fabryka (UC-INW-07, Long Track): utworzenie slotu dla zamówienia.
    public static ProductionSlot createForOrder(OrderId orderId, String[] specCodes) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        if (specCodes == null || specCodes.length == 0) {
            throw new IllegalArgumentException("specCodes must not be empty.");
        }
        return new ProductionSlot(SlotId.generate(), orderId);
    }

    // UC-INW-04: aktualizacja statusu/ETA z fabryki -> emisja DeliveryEtaUpdatedEvent.
    public void updateFactoryStatus(FactoryStatus status, LocalDate eta) {
        if (status == null) {
            throw new IllegalArgumentException("status must not be null.");
        }
        if (this.state == SlotState.CANCELLED) {
            throw new IllegalStateException("Cannot update a CANCELLED production slot.");
        }
        this.state = mapToSlotState(status);
        if (eta != null) {
            this.estimatedDelivery = eta;
            registerEvent(new DeliveryEtaUpdatedEvent(
                    UUID.randomUUID(), this.orderId.value(), eta, Instant.now()));
        }
    }

    // UC-INW-06: anulowanie slotu po zerwaniu kontraktu.
    public void cancelSlot() {
        this.state = SlotState.CANCELLED;
    }

    private SlotState mapToSlotState(FactoryStatus status) {
        if (status == FactoryStatus.IN_PRODUCTION) {
            return SlotState.IN_PRODUCTION;
        }
        if (status == FactoryStatus.SHIPPED) {
            return SlotState.IN_TRANSIT;
        }
        // REGISTERED / DELIVERED traktujemy jako "zaplanowany" do czasu przejęcia przez InventoryVehicle.
        return SlotState.SCHEDULED;
    }

    public void assignFactoryJobId(String factoryJobId) {
        if (factoryJobId == null || factoryJobId.isBlank()) {
            throw new IllegalArgumentException("factoryJobId must not be blank.");
        }
        this.factoryJobId = factoryJobId;
    }

    public SlotId getId() {
        return this.id;
    }

    public OrderId getOrderId() {
        return this.orderId;
    }

    public String getFactoryJobId() {
        return this.factoryJobId;
    }

    public LocalDate getEstimatedDelivery() {
        return this.estimatedDelivery;
    }

    public SlotState getState() {
        return this.state;
    }
}
