package salon.sales.application.service;

import salon.sales.application.port.in.ActivateOrderOnDepositUseCase;
import salon.sales.application.port.in.CancelOrderCommand;
import salon.sales.application.port.in.CancelOrderUseCase;
import salon.sales.application.port.out.OrderRepository;
import salon.sales.domain.model.order.Order;
import salon.shared.application.EventPublisherPort;
import salon.shared.event.DomainEvent;
import salon.shared.model.OrderId;

import java.util.Optional;

/**
 * Realizuje UC-SPR-02 (aktywacja po zadatku) i UC-SPR-03 (anulowanie).
 * Publikuje zdarzenia WYGENEROWANE PRZEZ AGREGAT — decyzja "jakie zdarzenie" siedzi w domenie
 * (Order.activate / Order.cancelOrder), serwis nie zagląda już do stanu/powodu anulacji.
 */
public class OrderAppService implements CancelOrderUseCase, ActivateOrderOnDepositUseCase {

    private final OrderRepository orderRepository;
    private final EventPublisherPort eventPublisher;

    public OrderAppService(OrderRepository orderRepository, EventPublisherPort eventPublisher) {
        if (orderRepository == null) {
            throw new IllegalArgumentException("orderRepository must not be null.");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        this.orderRepository = orderRepository;
        this.eventPublisher = eventPublisher;
    }

    // UC-SPR-02, krok 5: ZadatekZaksiegowany -> aktywacja zamówienia + ogłoszenie ZamowienieAktywowane.
    @Override
    public void activateOnDeposit(String orderId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        Optional<Order> found = orderRepository.findById(new OrderId(orderId));
        if (found.isEmpty()) {
            System.out.println("[OrderAppService] No order for id " + orderId + " — deposit ignored.");
            return;
        }
        Order order = found.get();
        order.activate();
        orderRepository.save(order);

        publishEventsOf(order);
    }

    // UC-SPR-03: anulowanie + (wg winy) polecenie do Rozliczeń: zatrzymaj zadatek / zleć zwrot.
    @Override
    public void cancelOrder(CancelOrderCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null.");
        }
        Optional<Order> found = orderRepository.findById(new OrderId(command.orderId()));
        if (found.isEmpty()) {
            throw new IllegalStateException("Order not found: " + command.orderId());
        }
        Order order = found.get();

        // Cała reguła (wydanie + wybór zdarzenia wg winy) jest w agregacie — tu tylko orkiestrujemy.
        order.cancelOrder(command.reason(), command.isHandedOver());
        orderRepository.save(order);

        publishEventsOf(order);
    }

    private void publishEventsOf(Order order) {
        for (DomainEvent event : order.pullDomainEvents()) {
            eventPublisher.publish(event);
        }
    }
}
