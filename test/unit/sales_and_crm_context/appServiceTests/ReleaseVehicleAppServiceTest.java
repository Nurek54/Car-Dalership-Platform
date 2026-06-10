package unit.sales_and_crm_context.appServiceTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.sales.application.port.out.InventoryIntegrationPort;
import salon.sales.application.service.SalesAppService;
import salon.sales.application.port.out.OrderRepository;
import salon.shared.application.EventPublisherPort;
import salon.sales.domain.model.order.*;
import salon.shared.model.OrderId;
import salon.sales.domain.model.offer.OfferId;
import salon.sales.domain.exception.InventoryLockedException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** UC-CRM-05: Rejestracja fizycznego wydania pojazdu */
@ExtendWith(MockitoExtension.class)
class ReleaseVehicleAppServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private InventoryIntegrationPort inventoryPort;
    @Mock private EventPublisherPort eventPublisher;

    @InjectMocks private SalesAppService salesAppService;

    @Test
    void shouldExecuteConfirmHandoverUseCaseSuccessfully() { // SCENARIUSZ GŁÓWNY
        // Kompletne zamówienie w bazie
        OrderId orderId = new OrderId("ORD-999");
        Order order = new Order(orderId, new OfferId("OFF-999"));
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
        Order order = new Order(orderId, new OfferId("OFF-999"));
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