package salon.billing.infrastructure.integration;

import salon.billing.application.port.out.CrmIntegrationPort;
import salon.billing.domain.model.document.BuyerDetails;
import salon.sales.application.port.out.CustomerRepository;
import salon.sales.application.port.out.OfferRepository;
import salon.sales.application.port.out.OrderRepository;
import salon.sales.domain.model.customer.Customer;
import salon.sales.domain.model.offer.Offer;
import salon.sales.domain.model.order.Order;
import salon.shared.model.OrderId;

/**
 * Adapter wyjściowy (ACL) portu {@link CrmIntegrationPort} — realizuje synchroniczne
 * zapytanie (Query) do Kontekstu Sprzedaży i CRM (PDF rozdz. 3.7.3 "CrmIntegrationPort").
 *
 * Zdarzenia logistyczne z placu (VehicleIsNotOnStock, VehicleReservedFromStock) niosą
 * wyłącznie orderId/VIN — bez danych osobowych (zgodność z RODO). Pełne dane nabywcy
 * dociągane są ścieżką: zamówienie -> oferta źródłowa -> tożsamość klienta -> agregat
 * Klient, i tłumaczone na lokalny obiekt wartości BuyerDetails.
 *
 * W środowisku rozproszonym zastąpiłby go klient REST do modułu CRM — kontrakt portu
 * pozostaje bez zmian.
 */
public class SalesCrmIntegrationAdapter implements CrmIntegrationPort {

    private final OrderRepository orderRepository;
    private final OfferRepository offerRepository;
    private final CustomerRepository customerRepository;

    public SalesCrmIntegrationAdapter(OrderRepository orderRepository,
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
    public BuyerDetails getCustomerDetails(OrderId orderId) {
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

        // Translacja modelu CRM na lokalny obiekt wartości kontekstu Fakturowania.
        return new BuyerDetails(customer.getFullName(), customer.getNip());
    }
}
