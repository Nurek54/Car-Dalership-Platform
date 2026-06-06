package unit.sales_and_crm_context.events;

import org.junit.jupiter.api.Test;
import salon.sales.domain.model.offer.CustomerId;
import salon.sales.domain.model.offer.Offer;
import salon.sales.domain.model.offer.OfferId;
import salon.sales.domain.model.order.CancellationReason;
import salon.sales.domain.model.order.Order;
import salon.shared.model.Money;
import salon.shared.model.OrderId;
import salon.shared.model.SpecificationId;

import java.math.BigDecimal;

class SalesEventTest {

    @Test
    void shouldEmitOrderPlacedEventWhenOrderIsCreatedAndSigned() {
        // Arrange
        Offer offer = new Offer(new OfferId("OFF-1"), new CustomerId("C-1"), new SpecificationId("S-1"));
        offer.publish(); // Zakładamy, że to emituje OfferPublishedEvent

        // Act - Klient podpisuje umowę (UC-SPR-02)
        Order order = Order.createFromOffer(offer);
        order.confirmSignature("DOCUSIGN-REF-123");

        // Assert
        assertThat(order.getDomainEvents())
                .hasAtLeastOneElementOfType(OrderPlacedEvent.class);
    }

    @Test
    void shouldEmitOrderCancelledEventWithReasonWhenCancelled() {
        // Arrange
        Order order = new Order(new OrderId("ORD-1"), new OfferId("OFF-1"));

        // Act - Twarde anulowanie z winy klienta (UC-SPR-03)
        order.cancelOrder(CancellationReason.CLIENT_FAULT, false);

        // Assert
        assertThat(order.getDomainEvents())
                .hasAtLeastOneElementOfType(OrderCancelledEvent.class);

        // Możemy też sprawdzić, czy zdarzenie ma odpowiednie dane
        OrderCancelledEvent event = (OrderCancelledEvent) order.getDomainEvents().get(0);
        assertThat(event.getReason()).isEqualTo("CLIENT_FAULT");
    }

    @Test
    void shouldEmitVehicleHandedOverEventWhenHandoverCompletes() {
        // Arrange
        Order order = new Order(new OrderId("ORD-2"), new OfferId("OFF-2"));
        // Zakładamy, że saldo to 0 i PDI jest gotowe...

        // Act - Wydanie auta (UC-SPR-08)
        order.completeHandover();

        // Assert
        assertThat(order.getDomainEvents())
                .hasAtLeastOneElementOfType(VehicleHandedOverEvent.class);
    }

    @Test
    void shouldCategorizeAsDepositAndEmitEventWhenPaymentCoversRequiredAmount() {
        // Arrange
        Money requiredDeposit = Money.of(new BigDecimal("5000.00"), "PLN");
        Payment payment = new Payment(
                new PaymentId("PAY-001"),
                new OrderId("ORD-123"),
                Money.of(new BigDecimal("5000.00"), "PLN")
        );

        // Act
        payment.categorizePayment(requiredDeposit);

        // Assert. TESTOWANIE ZDARZEŃ (Events)
        // Sprawdzamy, czy agregat poprawnie zapisał zdarzenie do swojego "notesu"
        assertThat(payment.getDomainEvents())
                .hasSize(1) // Czy wygenerowano dokładnie 1 zdarzenie?
                .first()    // Pobierz pierwsze zdarzenie
                .isInstanceOf(DepositRegisteredEvent.class); // Czy jest to odpowiednia klasa zdarzenia?
    }
}