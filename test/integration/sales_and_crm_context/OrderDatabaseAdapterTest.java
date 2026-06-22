package integration.sales_and_crm_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import salon.sales.infrastructure.out.persistence.OrderDatabaseAdapter;
import salon.sales.application.domain.model.order.Order;
import salon.sales.application.domain.model.order.PaymentStatus;
import salon.sales.application.domain.model.offer.OfferId;
import salon.common.model.OrderId;
import salon.common.model.Money;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(OrderDatabaseAdapter.class)
class OrderDatabaseAdapterTest {

    @Autowired private OrderDatabaseAdapter databaseAdapter;

    @Test
    void shouldSaveAndLoadOrderFromSqlDatabase() {
        // A valid Order Aggregate
        OrderId orderId = new OrderId("ORD-DB-1");
        Order order = new Order(orderId, new OfferId("OFF-1"), Money.of(100000, "PLN"));

        // Wykonujemy fizyczny zapis
        databaseAdapter.save(order);
        Optional<Order> loadedOrder = databaseAdapter.findById(orderId);

        // The aggregate is correctly read from the database
        assertThat(loadedOrder).isPresent();
        assertThat(loadedOrder.get().id()).isEqualTo(orderId);
        assertThat(loadedOrder.get().requiredDeposit()).isEqualTo(Money.of(100000, "PLN"));
    }

    @Test
    void shouldThrowOptimisticLockingExceptionWhenConcurrentModificationOccurs() {
        // We save the order to the database
        OrderId orderId = new OrderId("ORD-DB-2");
        Order initialOrder = new Order(orderId, new OfferId("OFF-2"), Money.of(50000, "PLN"));
        databaseAdapter.save(initialOrder);

        // Simulation: two separate processes/users load the same order from the database
        Order user1Copy = databaseAdapter.findById(orderId).orElseThrow();
        Order user2Copy = databaseAdapter.findById(orderId).orElseThrow();

        // User 1 makes changes and saves correctly
        user1Copy.activate();
        databaseAdapter.save(user1Copy);

        // User 2, unaware of the changes, tries to save their "older" copy
        user2Copy.changePaymentStatus(PaymentStatus.PAID);

        // The database rejects user 2's save due to a version conflict
        assertThatThrownBy(() -> databaseAdapter.save(user2Copy))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
