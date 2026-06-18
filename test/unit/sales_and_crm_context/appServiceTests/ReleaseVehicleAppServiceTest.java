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

/** UC-CRM-05: Rejestracja fizycznego wydania pojazdu */
@ExtendWith(MockitoExtension.class)
class ReleaseVehicleAppServiceTest {

    @Mock private OrderDatabaseRepository orderRepository;
    @Mock private InventoryIntegration inventoryPort;
    @Mock private EventPublisher eventPublisher;

    @InjectMocks private SalesService salesAppService;

    @Test
    void shouldExecuteConfirmHandoverUseCaseSuccessfully() { // SCENARIUSZ GŁÓWNY
        // Kompletne zamówienie w bazie
        OrderId orderId = new OrderId("ORD-999");
        Order order = new Order(orderId, new OfferId("OFF-999"), Money.of(150000, "PLN"));
        order.activate();
        order.markAsReadyForHandover();
        order.scheduleHandover(LocalDate.now());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        salesAppService.confirmHandover(orderId);

        // Zlecamy portowi Inwentarza zdjęcie fizycznego auta z magazynu/placu
        verify(inventoryPort).releasePhysicalVehicle(order.getVehicleId());
        // Zapisujemy zamówienie jako Zrealizowane
        assertThat(order.getState()).isEqualTo(OrderState.COMPLETED);
        verify(orderRepository).save(order);
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    void shouldHandleInventoryLockErrorAndRollback() {
        // Wydanie umówione
        OrderId orderId = new OrderId("ORD-999");
        Order order = new Order(orderId, new OfferId("OFF-999"), Money.of(150000, "PLN"));
        order.activate();
        order.markAsReadyForHandover();
        order.scheduleHandover(LocalDate.now());

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Auto zablokowane
        doThrow(new InventoryLockedException("Vehicle blocked by logistics on parking lot"))
                .when(inventoryPort).releasePhysicalVehicle(any());

        // Proces wydania przerywa się
        assertThatThrownBy(() -> salesAppService.confirmHandover(orderId))
                .isInstanceOf(InventoryLockedException.class);

        // System nie może zapisać tego zamówienia jako zrealizowane!
        verify(orderRepository, never()).save(argThat(savedOrder -> savedOrder.getState() == OrderState.COMPLETED));

        verify(eventPublisher, never()).publishAll(anyList());
    }
}