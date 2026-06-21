package unit.sales_and_crm_context.appServiceTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.sales.application.command.ScheduleHandoverCommand;
import salon.sales.application.service.SalesService;
import salon.sales.application.port.out.OrderDatabaseRepository;
import salon.common.application.EventPublisher;
import salon.sales.application.domain.model.order.Order;
import salon.common.model.Money;
import salon.common.model.OrderId;
import salon.sales.application.domain.model.offer.OfferId;
import salon.sales.application.domain.exception.OrderNotFoundException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** UC-CRM-04: Handling the customer's invitation for pickup */
@ExtendWith(MockitoExtension.class)
class ScheduleHandoverAppServiceTest {

    @Mock private OrderDatabaseRepository orderRepository;
    @Mock private EventPublisher eventPublisher;

    @InjectMocks private SalesService salesAppService;

    @Test
    void shouldScheduleHandoverAndSaveOrder() { // MAIN SCENARIO
        // The order exists in the database and is ready for handover
        String rawOrderId = "ORD-11";
        OrderId orderId = new OrderId(rawOrderId);
        Order order = new Order(orderId, new OfferId("OFF-11"), Money.of(150000, "PLN"));
        order.activate();
        order.markAsReadyForHandover(); // Status: READY_FOR_HANDOVER
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        ScheduleHandoverCommand command = new ScheduleHandoverCommand(rawOrderId, LocalDate.now().plusDays(3));
        salesAppService.scheduleHandover(command);

        // The new order state is saved in the database
        verify(orderRepository).save(order);
        // Events about scheduling the visit are sent
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    void shouldFailToScheduleHandoverInThePast() {
        // A valid order in the database
        String rawOrderId = "ORD-12";
        OrderId orderId = new OrderId(rawOrderId);
        Order order = new Order(orderId, new OfferId("OFF-12"), Money.of(150000, "PLN"));
        order.activate();
        order.markAsReadyForHandover();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // The Salesperson enters an invalid past date in the form
        LocalDate pastDate = LocalDate.now().minusDays(5);

        // The application rejects the request (input validation before the database layer)
        ScheduleHandoverCommand command = new ScheduleHandoverCommand(rawOrderId, LocalDate.now().minusDays(5));
        assertThatThrownBy(() -> salesAppService.scheduleHandover(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Handover date cannot be in the past");

        // The order in the database remains intact
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenOrderDoesNotExist() {
        // The database does not have the indicated order
        String rawOrderId = "ORD-UNKNOWN";
        OrderId fakeId = new OrderId(rawOrderId);
        when(orderRepository.findById(fakeId)).thenReturn(Optional.empty());

        // The application throws an exception
        ScheduleHandoverCommand command = new ScheduleHandoverCommand(rawOrderId, LocalDate.now().plusDays(1));
        assertThatThrownBy(() -> salesAppService.scheduleHandover(command))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order with ID ORD-UNKNOWN not found");

        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publishAll(any());
    }
}