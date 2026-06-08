package integration.driven_adapters.database_adapter.sales;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import salon.sales.domain.model.order.Order;
import salon.shared.model.OrderId;
import salon.sales.domain.model.order.OrderState;
import salon.sales.domain.model.order.CancellationReason;
import salon.sales.domain.model.offer.OfferId;
import salon.shared.model.Money;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(OrderDatabaseAdapter.class)
class OrderDatabaseAdapterTest {

    @Autowired
    private OrderDatabaseAdapter adapter;

    // 1. ZAPIS, ODCZYT I MAPOWANIE: Symulacja anulowanego zamówienia
    @Test
    void shouldSaveAndRetrieveCancelledOrderWithReasonMapped() {
        // Arrange
        OrderId orderId = new OrderId("ORD-2026-005");
        OfferId sourceOfferId = new OfferId("OFF-2026-001");
        Money requiredDeposit = Money.of(new BigDecimal("5000.00"), "PLN");

        // Tworzymy zamówienie na podstawie oferty (symulacja factory method)
        Order order = new Order(orderId, sourceOfferId, requiredDeposit);

        // Symulujemy anulowanie zamówienia z winy klienta, zanim auto zostało wydane
        order.cancelOrder(CancellationReason.CLIENT_FAULT, false);

        // Act
        adapter.save(order);
        Optional<Order> retrievedOrder = adapter.findById(orderId);

        // Assert
        assertThat(retrievedOrder).isPresent();
        Order retrieved = retrievedOrder.get();

        assertThat(retrieved.getId()).isEqualTo(orderId);
        assertThat(retrieved.getSourceOfferId()).isEqualTo(sourceOfferId);

        // Weryfikacja wymaganego zadatku
        assertThat(retrieved.getRequiredDeposit().getAmount()).isEqualByComparingTo("5000.00");

        // Weryfikacja mapowania Enumów stanu i powodu rezygnacji
        assertThat(retrieved.getState()).isEqualTo(OrderState.CANCELLED);
        assertThat(retrieved.getCancellationReason()).isEqualTo(CancellationReason.CLIENT_FAULT);
    }

    // 2. BRAK DANYCH
    @Test
    void shouldReturnEmptyOptionalWhenOrderDoesNotExist() {
        // Act
        Optional<Order> result = adapter.findById(new OrderId("ORD-UNKNOWN"));

        // Assert
        assertThat(result).isEmpty();
    }
}