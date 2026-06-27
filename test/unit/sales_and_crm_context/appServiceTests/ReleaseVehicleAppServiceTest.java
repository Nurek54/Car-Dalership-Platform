package unit.sales_and_crm_context.appServiceTests;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.common.application.EventPublisher;
import salon.common.model.Money;
import salon.common.model.OrderId;
import salon.sales.application.domain.exception.InventoryLockedException;
import salon.sales.application.domain.model.offer.OfferFactory;
import salon.sales.application.domain.model.offer.OfferId;
import salon.sales.application.domain.model.order.Order;
import salon.sales.application.domain.model.order.OrderFactory;
import salon.sales.application.domain.model.order.OrderState;
import salon.sales.application.port.out.BillingIntegration;
import salon.sales.application.port.out.CustomerDatabaseRepository;
import salon.sales.application.port.out.InventoryIntegration;
import salon.sales.application.port.out.OfferDatabaseRepository;
import salon.sales.application.port.out.OrderDatabaseRepository;
import salon.sales.application.port.out.SpecificationPriceReadModelPort;
import salon.sales.application.service.SalesService;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** UC-CRM-05: Rejestracja fizycznego wydania pojazdu */
@ExtendWith(MockitoExtension.class)
class ReleaseVehicleAppServiceTest {

    @Mock private CustomerDatabaseRepository customerRepository;
    @Mock private OfferDatabaseRepository offerRepository;
    @Mock private OrderDatabaseRepository orderRepository;
    @Mock private BillingIntegration billingIntegration;
    @Mock private InventoryIntegration inventoryPort;
    @Mock private EventPublisher eventPublisher;
    @Mock private SpecificationPriceReadModelPort specificationPriceReadModel;

    private SalesService salesAppService;

    @BeforeEach
    void setUp() {
        salesAppService = new SalesService(customerRepository, offerRepository, orderRepository,
                billingIntegration, inventoryPort, eventPublisher,
                new OfferFactory(), new OrderFactory(), specificationPriceReadModel);
    }

    private Order scheduledOrder(String rawOrderId, String rawOfferId) {
        Order order = new Order(new OrderId(rawOrderId), new OfferId(rawOfferId), Money.of(150000, "PLN"));
        order.activate();
        order.markAsReadyForHandover();
        order.scheduleHandover(LocalDate.now());
        return order;
    }

    @Test
    void shouldExecuteConfirmHandoverUseCaseSuccessfully() { // SCENARIUSZ GŁÓWNY
        // Kompletne zamówienie w bazie
        OrderId orderId = new OrderId("ORD-999");
        Order order = scheduledOrder("ORD-999", "OFF-999");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        salesAppService.confirmHandover(orderId);

        // Instruujemy port Magazynu, aby usunął fizyczny samochód z magazynu/placu
        verify(inventoryPort).releasePhysicalVehicle(order.id().value());
        // Zapisujemy zamówienie jako Completed
        assertThat(order.state()).isEqualTo(OrderState.COMPLETED);
        verify(orderRepository).save(order);
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    void shouldHandleInventoryLockErrorAndRollback() {
        // Wydanie jest umówione
        OrderId orderId = new OrderId("ORD-999");
        Order order = scheduledOrder("ORD-999", "OFF-999");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Samochód jest zablokowany
        doThrow(new InventoryLockedException("Vehicle blocked by logistics on parking lot"))
                .when(inventoryPort).releasePhysicalVehicle(any());

        // Proces wydania zostaje przerwany
        assertThatThrownBy(() -> salesAppService.confirmHandover(orderId))
                .isInstanceOf(InventoryLockedException.class);

        // System nie może zapisać tego zamówienia jako zakończonego!
        verify(orderRepository, never()).save(argThat(savedOrder -> savedOrder.state() == OrderState.COMPLETED));

        verify(eventPublisher, never()).publishAll(anyList());
    }
}
