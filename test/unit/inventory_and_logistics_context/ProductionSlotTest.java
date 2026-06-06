package unit.inventory_and_logistics_context;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.assertj.core.api.Assertions.*;

class ProductionSlotTest {

    @Test
    void shouldCreateProductionSlotWithInitialState() {
        // Act (When)
        ProductionSlot slot = ProductionSlot.createForOrder(
                new OrderId("ORD-500"),
                new String[]{"ENGINE_2.0", "WINTER_PACK"}
        );

        // Assert (Then)
        assertThat(slot.getState()).isEqualTo(SlotState.SCHEDULED);
        assertThat(slot.getOrderId()).isEqualTo(new OrderId("ORD-500"));
    }

    @Test
    void shouldUpdateFactoryStatusAndEstimatedDeliveryDate() {
        // Arrange (Given)
        ProductionSlot slot = ProductionSlot.createForOrder(
                new OrderId("ORD-501"),
                new String[]{"PAINT_BLACK"}
        );

        LocalDate newEstimatedDelivery = LocalDate.of(2026, 12, 1);

        // Act (When)
        // Symulacja nadejścia asynchronicznej aktualizacji z API fabryki (UC-INW-04)
        slot.updateFactoryStatus(SlotState.IN_PRODUCTION, newEstimatedDelivery);

        // Assert (Then)
        // Agregat musi poprawnie zaktualizować swój stan oraz estymowaną datę dostawy
        assertThat(slot.getState()).isEqualTo(SlotState.IN_PRODUCTION);
        assertThat(slot.getEstimatedDelivery()).isEqualTo(newEstimatedDelivery);
    }

    @Test
    void shouldTransitionToCancelledStateWhenSlotIsCancelled() {
        // Arrange (Given)
        ProductionSlot slot = ProductionSlot.createForOrder(
                new OrderId("ORD-502"),
                new String[]{"SUNROOF"}
        );

        // Act (When)
        // Symulacja anulowania kolejki w fabryce w wyniku zerwania kontraktu (UC-INW-06)
        slot.cancelSlot();

        // Assert (Then)
        assertThat(slot.getState()).isEqualTo(SlotState.CANCELLED);
    }
}