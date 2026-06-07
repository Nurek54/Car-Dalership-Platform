package integration.driven_adapters.database_adapter.logistics;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import salon.inventory.domain.model.ProductionSlot;
import salon.inventory.domain.model.SlotId;
import salon.inventory.domain.model.SlotState;
import salon.shared.model.OrderId;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(ProductionSlotDatabaseAdapter.class)
class ProductionSlotDatabaseAdapterTest {

    @Autowired
    private ProductionSlotDatabaseAdapter adapter;

    // 1. ZAPIS, ODCZYT I MAPOWANIE: Tworzenie slotu dla zamówienia
    @Test
    void shouldSaveAndRetrieveProductionSlotForGivenOrder() {
        // Arrange
        OrderId orderId = new OrderId("ORD-999");
        String[] specCodes = {"ENGINE_2.0", "PAINT_RED"};

        // Wykorzystujemy metodę fabrykującą (Factory Method) zgodną z dokumentacją
        ProductionSlot slot = ProductionSlot.createForOrder(orderId, specCodes);

        // Symulujemy aktualizację statusu z fabryki (wzorzec ACL) z nową datą dostawy
        LocalDate estimatedDelivery = LocalDate.of(2026, 12, 1);
        slot.updateFactoryStatus(FactoryStatus.IN_PRODUCTION, estimatedDelivery);

        // Act
        adapter.save(slot);
        Optional<ProductionSlot> retrievedSlot = adapter.findById(slot.getId());

        // Assert
        assertThat(retrievedSlot).isPresent();
        ProductionSlot retrieved = retrievedSlot.get();

        // Weryfikacja powiązania slotu z zamówieniem klienta
        assertThat(retrieved.getOrderId()).isEqualTo(orderId);

        // Weryfikacja mapowania statusu slotu
        assertThat(retrieved.getState()).isEqualTo(SlotState.IN_PRODUCTION);

        // Weryfikacja zapisu daty (mapowanie LocalDate do typu Date/Timestamp w bazie)
        assertThat(retrieved.getEstimatedDelivery()).isEqualTo(estimatedDelivery);
    }

    // 2. BRAK DANYCH
    @Test
    void shouldReturnEmptyOptionalWhenProductionSlotDoesNotExist() {
        // Act
        Optional<ProductionSlot> result = adapter.findById(new SlotId("SLOT-UNKNOWN"));

        // Assert
        assertThat(result).isEmpty();
    }
}