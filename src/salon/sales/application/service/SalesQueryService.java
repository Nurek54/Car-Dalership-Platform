package salon.sales.application.service;

import org.springframework.stereotype.Service;
import salon.sales.api.CustomerSnapshotDto;
import salon.sales.api.OfferSnapshotDto;
import salon.sales.api.SalesQueryFacade;
import salon.sales.application.port.out.CustomerRepository;
import salon.sales.application.port.out.OfferRepository;
import salon.sales.application.port.out.OrderRepository;
import salon.sales.domain.model.customer.Customer;
import salon.sales.domain.model.offer.Offer;
import salon.sales.domain.model.order.Order;
import salon.shared.model.OrderId;

/**
 * Implementacja fasady zapytań {@link SalesQueryFacade} Kontekstu Sprzedaży i CRM.
 *
 * Nawigacja zamówienie -> oferta źródłowa -> tożsamość klienta -> agregat Klient
 * jest wiedzą domenową Sprzedaży i pozostaje w całości wewnątrz tego kontekstu;
 * na zewnątrz wychodzi wyłącznie {@link CustomerSnapshotDto} (Published Language).
 */
@Service
public class SalesQueryService implements SalesQueryFacade {

    private final OrderRepository orderRepository;
    private final OfferRepository offerRepository;
    private final CustomerRepository customerRepository;

    public SalesQueryService(OrderRepository orderRepository,
                             OfferRepository offerRepository,
                             CustomerRepository customerRepository) {
        if (orderRepository == null) {
            throw new IllegalArgumentException("orderRepository must not be null.");
        }
        if (offerRepository == null) {
            throw new IllegalArgumentException("offerRepository must not be null.");
        }
        if (customerRepository == null) {
            throw new IllegalArgumentException("customerRepository must not be null.");
        }
        this.orderRepository = orderRepository;
        this.offerRepository = offerRepository;
        this.customerRepository = customerRepository;
    }

    @Override
    public CustomerSnapshotDto findBuyerForOrder(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        Order order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException(
                        "No order in CRM for id " + orderId.value()));
        Offer offer = this.offerRepository.findById(order.getOfferId())
                .orElseThrow(() -> new IllegalStateException(
                        "No source offer in CRM for order " + orderId.value()));
        Customer customer = this.customerRepository.findById(offer.getCustomerId())
                .orElseThrow(() -> new IllegalStateException(
                        "No customer in CRM for id " + offer.getCustomerId().value()));
        return new CustomerSnapshotDto(customer.getFullName(), customer.getNip());
    }

    @Override
    public OfferSnapshotDto findOfferForOrder(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        Order order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException(
                        "No order in CRM for id " + orderId.value()));
        Offer offer = this.offerRepository.findById(order.getOfferId())
                .orElseThrow(() -> new IllegalStateException(
                        "No source offer in CRM for order " + orderId.value()));
        if (offer.getFinalPrice() == null) {
            throw new IllegalStateException(
                    "Offer " + offer.getId().value() + " has no final price yet.");
        }
        return new OfferSnapshotDto(offer.getId().value(), offer.getFinalPrice());
    }
}
