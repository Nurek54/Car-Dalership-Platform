package unit.sales_and_crm_context.appServiceTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.common.application.EventPublisher;
import salon.common.model.Money;
import salon.sales.application.domain.exception.SpecificationNotFoundException;
import salon.sales.application.domain.model.offer.Offer;
import salon.sales.application.domain.model.offer.OfferFactory;
import salon.sales.application.domain.model.order.OrderFactory;
import salon.sales.application.port.out.BillingIntegration;
import salon.sales.application.port.out.CustomerDatabaseRepository;
import salon.sales.application.port.out.InventoryIntegration;
import salon.sales.application.port.out.OfferDatabaseRepository;
import salon.sales.application.port.out.OrderDatabaseRepository;
import salon.sales.application.port.out.SpecificationPriceReadModelPort;
import salon.sales.application.service.SalesService;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** UC-CRM-02: Generating the proforma offer (price from the read model fed by an event). */
@ExtendWith(MockitoExtension.class)
class GenerateOfferAppServiceTest {

    @Mock private CustomerDatabaseRepository customerRepository;
    @Mock private OfferDatabaseRepository offerRepository;
    @Mock private OrderDatabaseRepository orderRepository;
    @Mock private BillingIntegration billingIntegration;
    @Mock private InventoryIntegration inventoryIntegration;
    @Mock private EventPublisher eventPublisher;
    @Mock private SpecificationPriceReadModelPort specificationPriceReadModel;

    private SalesService salesAppService;

    @BeforeEach
    void setUp() {
        salesAppService = new SalesService(customerRepository, offerRepository, orderRepository,
                billingIntegration, inventoryIntegration, eventPublisher,
                new OfferFactory(), new OrderFactory(), specificationPriceReadModel);
    }

    @Test
    void shouldGenerateAndSaveOfferSuccessfully() {
        // The local read model already knows the specification pricing (it arrived via the SpecificationCompleted event)
        when(specificationPriceReadModel.findPrice(any()))
                .thenReturn(Optional.of(Money.of(150000, "PLN")));

        // We invoke the offer-generation use case
        salesAppService.generateOffer("CUST-1", "SPEC-1");

        // The new offer is correctly constructed and passed for saving in the repository
        verify(offerRepository).save(any(Offer.class));
    }

    @Test
    void shouldFailToGenerateOfferWhenSpecificationPriceNotYetAvailable() {
        // The SpecificationCompleted event has not arrived yet — no pricing in the read model
        when(specificationPriceReadModel.findPrice(any())).thenReturn(Optional.empty());

        // The sales service rejects the process — no pricing = no offer
        assertThatThrownBy(() -> salesAppService.generateOffer("CUST-1", "SPEC-999"))
                .isInstanceOf(SpecificationNotFoundException.class);

        // No faulty offer is saved in our CRM database
        verify(offerRepository, never()).save(any());
    }
}
