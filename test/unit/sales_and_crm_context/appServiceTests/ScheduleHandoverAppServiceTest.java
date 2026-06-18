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

/** UC-CRM-04: Obsługa zaproszenia klienta po odbiór */
@ExtendWith(MockitoExtension.class)
class ScheduleHandoverAppServiceTest {

    @Mock private OrderDatabaseRepository orderRepository;
    @Mock private EventPublisher eventPublisher;

    @InjectMocks private SalesService salesAppService;

    @Test
    void shouldScheduleHandoverAndSaveOrder() { // SCENARIUSZ GŁÓWNY
        // Zamówienie istnieje w bazie i jest gotowe do wydania
        String rawOrderId = "ORD-11";
        OrderId orderId = new OrderId(rawOrderId);
        Order order = new Order(orderId, new OfferId("OFF-11"), Money.of(150000, "PLN"));
        order.activate();
        order.markAsReadyForHandover(); // Status: READY_FOR_HANDOVER
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        ScheduleHandoverCommand command = new ScheduleHandoverCommand(rawOrderId, LocalDate.now().plusDays(3));
        salesAppService.scheduleHandover(command);

        // Nowy stan zamówienia zostaje zapisany w bazie danych
        verify(orderRepository).save(order);
        // Zderzania o omówieniu wizyty są wysyłane
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    void shouldFailToScheduleHandoverInThePast() {
        // Poprawne zamówienie w bazie
        String rawOrderId = "ORD-12";
        OrderId orderId = new OrderId(rawOrderId);
        Order order = new Order(orderId, new OfferId("OFF-12"), Money.of(150000, "PLN"));
        order.activate();
        order.markAsReadyForHandover();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Handlowiec wpisuje w formularzu błędną datę z przeszłości
        LocalDate pastDate = LocalDate.now().minusDays(5);

        // Aplikacja odrzuca żądanie (walidacja na wejściu przed warstwą bazy danych)
        ScheduleHandoverCommand command = new ScheduleHandoverCommand(rawOrderId, LocalDate.now().minusDays(5));
        assertThatThrownBy(() -> salesAppService.scheduleHandover(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Handover date cannot be in the past");

        // Zamówienie w bazie pozostaje nienaruszone
        verify(orderRepository, never()).save(any());
    }

    @Test
    void shouldThrowExceptionWhenOrderDoesNotExist() {
        // Baza danych nie ma wskazanego zamówienia
        String rawOrderId = "ORD-UNKNOWN";
        OrderId fakeId = new OrderId(rawOrderId);
        when(orderRepository.findById(fakeId)).thenReturn(Optional.empty());

        // Aplikacja rzuca wyjątkiem
        ScheduleHandoverCommand command = new ScheduleHandoverCommand(rawOrderId, LocalDate.now().plusDays(1));
        assertThatThrownBy(() -> salesAppService.scheduleHandover(command))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("Order with ID ORD-UNKNOWN not found");

        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publishAll(any());
    }
}