package unit.sales_and_crm_context.appServiceTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.sales.application.domain.model.order.Order;
import salon.sales.application.domain.model.order.OrderState;
import salon.sales.application.port.out.InventoryIntegration;
import salon.sales.application.service.SalesService;
import salon.sales.application.port.out.OrderDatabaseRepository;
import salon.common.application.EventPublisher;
import salon.common.model.Money;
import salon.common.model.OrderId;
import salon.sales.application.domain.model.offer.OfferId;
import salon.sales.application.domain.exception.InventoryLockedException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** UC-CRM-05: Registering the physical vehicle handover */
@ExtendWith(MockitoExtension.class)
class ReleaseVehicleAppServiceTest {

    @Mock private OrderDatabaseRepository orderRepository;
    @Mock private InventoryIntegration inventoryPort;
    @Mock private EventPublisher eventPublisher;

    @InjectMocks private SalesService salesAppService;

    @Test
    void shouldExecuteConfirmHandoverUseCaseSuccessfully() { // MAIN SCENARIO
        // A complete order in the database
        OrderId orderId = new OrderId("ORD-999");
        Order order = new Order(orderId, new OfferId("OFF-999"), Money.of(150000, "PLN"));
        order.activate();
        order.markAsReadyForHandover();
        order.scheduleHandover(LocalDate.now());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        salesAppService.confirmHandover(orderId);

        // We instruct the Inventory port to remove the physical car from the warehouse/yard
        verify(inventoryPort).releasePhysicalVehicle(order.vehicleId());
        // We save the order as Completed
        assertThat(order.state()).isEqualTo(OrderState.COMPLETED);
        verify(orderRepository).save(order);
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    void shouldHandleInventoryLockErrorAndRollback() {
        // The handover is scheduled
        OrderId orderId = new OrderId("ORD-999");
        Order order = new Order(orderId, new OfferId("OFF-999"), Money.of(150000, "PLN"));
        order.activate();
        order.markAsReadyForHandover();
        order.scheduleHandover(LocalDate.now());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // The car is locked
        doThrow(new InventoryLockedException("Vehicle blocked by logistics on parking lot"))
                .when(inventoryPort).releasePhysicalVehicle(any());

        // The handover process aborts
        assertThatThrownBy(() -> salesAppService.confirmHandover(orderId))
                .isInstanceOf(InventoryLockedException.class);

        // The system must not save this order as completed!
        verify(orderRepository, never()).save(argThat(savedOrder -> savedOrder.state() == OrderState.COMPLETED));

        verify(eventPublisher, never()).publishAll(anyList());
    }
}