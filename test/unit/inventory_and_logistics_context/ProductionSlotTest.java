package unit.inventory_and_logistics_context;

import org.junit.jupiter.api.Test;
import salon.logistics.domain.model.slot.FactoryStatus;
import salon.logistics.domain.model.slot.ProductionSlot;
import salon.logistics.domain.model.slot.SlotState;
import salon.shared.model.OrderId;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class ProductionSlotTest {

    @Test
    void shouldCreateProductionSlotWithInitialState() {
        ProductionSlot slot = ProductionSlot.createForOrder(
                new OrderId("ORD-500"),
                new String[]{"ENGINE_2.0", "WINTER_PACK"}
        );

        assertThat(slot.getState()).isEqualTo(SlotState.SCHEDULED);
        assertThat(slot.getOrderId()).isEqualTo(new OrderId("ORD-500"));
    }

    @Test
    void shouldUpdateFactoryStatusAndEstimatedDeliveryDate() {
        ProductionSlot slot = ProductionSlot.createForOrder(
                new OrderId("ORD-501"),
                new String[]{"PAINT_BLACK"}
        );
        LocalDate newEstimatedDelivery = LocalDate.of(2026, 12, 1);

        // Asynchroniczna aktualizacja z API fabryki (UC-INW-04)
        slot.updateFactoryStatus(FactoryStatus.IN_PRODUCTION, newEstimatedDelivery);

        assertThat(slot.getState()).isEqualTo(SlotState.IN_PRODUCTION);
        assertThat(slot.getEstimatedDelivery()).isEqualTo(newEstimatedDelivery);
    }

    @Test
    void shouldTransitionToCancelledStateWhenSlotIsCancelled() {
        ProductionSlot slot = ProductionSlot.createForOrder(
                new OrderId("ORD-502"),
                new String[]{"SUNROOF"}
        );

        slot.cancelSlot();

        assertThat(slot.getState()).isEqualTo(SlotState.CANCELLED);
    }
}
