package salon.sales.application.service;

import org.springframework.stereotype.Service;
import salon.sales.api.CustomerSnapshotDto;
import salon.sales.api.OfferSnapshotDto;
import salon.sales.api.SalesQueryFacade;
import salon.sales.application.port.out.CustomerDatabaseRepository;
import salon.sales.application.port.out.OfferDatabaseRepository;
import salon.sales.application.port.out.OrderDatabaseRepository;
import salon.sales.application.domain.model.customer.Customer;
import salon.sales.application.domain.model.offer.Offer;
import salon.sales.application.domain.model.order.Order;
import salon.common.model.OrderId;

/**
 * Implementacja fasady zapytań {@link SalesQueryFacade} Kontekstu Sprzedaży i CRM.
 *
 * Nawigacja zamówienie -> oferta źródłowa -> tożsamość klienta -> agregat Klient
 * jest wiedzą domenową Sprzedaży i pozostaje w całości wewnątrz tego kontekstu;
 * na zewnątrz wychodzi wyłącznie {@link CustomerSnapshotDto} (Published Language).
 */
@Service
public class SalesQueryService implements SalesQueryFacade {

    private final OrderDatabaseRepository orderRepository;
    private final OfferDatabaseRepository offerRepository;
    private final CustomerDatabaseRepository customerRepository;

    public SalesQueryService(OrderDatabaseRepository orderRepository,
                             OfferDatabaseRepository offerRepository,
                             CustomerDatabaseRepository customerRepository) {
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
        Offer offer = this.offerRepository.findById(order.offerId())
                .orElseThrow(() -> new IllegalStateException(
                        "No source offer in CRM for order " + orderId.value()));
        Customer customer = this.customerRepository.findById(offer.customerId())
                .orElseThrow(() -> new IllegalStateException(
                        "No customer in CRM for id " + offer.customerId().value()));
        return new CustomerSnapshotDto(customer.fullName(), customer.nip());
    }

    @Override
    public OfferSnapshotDto findOfferForOrder(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        Order order = this.orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalStateException(
                        "No order in CRM for id " + orderId.value()));
        Offer offer = this.offerRepository.findById(order.offerId())
                .orElseThrow(() -> new IllegalStateException(
                        "No source offer in CRM for order " + orderId.value()));
        if (offer.finalPrice() == null) {
            throw new IllegalStateException(
                    "Offer " + offer.id().value() + " has no final price yet.");
        }
        return new OfferSnapshotDto(offer.id().value(), offer.finalPrice());
    }
}
