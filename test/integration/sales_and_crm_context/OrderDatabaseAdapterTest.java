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
        // Poprawny agregat Zamówienia
        OrderId orderId = new OrderId("ORD-DB-1");
        Order order = new Order(orderId, new OfferId("OFF-1"), Money.of(100000, "PLN"));

        // Wykonujemy fizyczny zapis
        databaseAdapter.save(order);
        Optional<Order> loadedOrder = databaseAdapter.findById(orderId);

        // Agregat jest poprawnie odczytany z bazy
        assertThat(loadedOrder).isPresent();
        assertThat(loadedOrder.get().id()).isEqualTo(orderId);
        assertThat(loadedOrder.get().requiredDeposit()).isEqualTo(Money.of(100000, "PLN"));
    }

    @Test
    void shouldThrowOptimisticLockingExceptionWhenConcurrentModificationOccurs() {
        // Zapisujemy zamówienie do bazy
        OrderId orderId = new OrderId("ORD-DB-2");
        Order initialOrder = new Order(orderId, new OfferId("OFF-2"), Money.of(50000, "PLN"));
        databaseAdapter.save(initialOrder);

        // Symulacja: dwa osobne procesy/użytkownicy wczytują to samo zamówienie z bazy
        Order user1Copy = databaseAdapter.findById(orderId).orElseThrow();
        Order user2Copy = databaseAdapter.findById(orderId).orElseThrow();

        // Użytkownik 1 wprowadza zmiany i poprawnie zapisuje
        user1Copy.activate();
        databaseAdapter.save(user1Copy);

        // Użytkownik 2, nieświadomy zmian, próbuje zapisać swoją „starszą" kopię
        user2Copy.changePaymentStatus(PaymentStatus.PAID);

        // Baza odrzuca zapis użytkownika 2 z powodu konfliktu wersji
        assertThatThrownBy(() -> databaseAdapter.save(user2Copy))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);
    }
}
