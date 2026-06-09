package integration.driving_adapetrs.event_subscriber_adapter.sales;

import salon.logistics.infrastructure.messaging.SalesEventSubscriberAdapter;
import salon.logistics.infrastructure.messaging.OrderActivatedEvent;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.logistics.application.InventoryAppService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesEventSubscriberAdapterTest {

    // Kaskader dla usługi aplikacji w Kontekście Inwentarza
    @Mock
    private InventoryAppService inventoryAppService;

    // Testowany adapter - wstrzykujemy do niego kaskadera
    @InjectMocks
    private SalesEventSubscriberAdapter adapter;

    // 1. HAPPY PATH
    @Test
    void shouldAllocateVehicleWhenValidOrderPlacedEventIsReceived() {
        // Arrange
        OrderActivatedEvent validEvent = new OrderActivatedEvent(
                "EVT-1001",
                "ORD-999",
                List.of("PAINT_BLACK", "ENGINE_2.0")
        );

        // Act
        adapter.handleOrderPlacedEvent(validEvent);

        // Assert - Upewniamy się, że adapter przekazał zadanie do warstwy aplikacji
        verify(inventoryAppService, times(1))
                .allocateVehicleForOrder("ORD-999", validEvent.specCodes());
    }

    // 2. WALIDACJA WEJŚCIA (Ochrona przed uszkodzonymi danymi)
    @Test
    void shouldRejectEventWhenOrderIdIsMissing() {
        // Arrange
        OrderActivatedEvent invalidEvent = new OrderActivatedEvent(
                "EVT-1002",
                null, // Zgubione ID zamówienia!
                List.of("PAINT_BLACK")
        );

        // Act & Assert
        assertThatThrownBy(() -> adapter.handleOrderPlacedEvent(invalidEvent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Identyfikator zamówienia (orderId) nie może być pusty");

        // Gwarancja, że zepsute dane nie dotarły do logiki biznesowej
        verify(inventoryAppService, never()).allocateVehicleForOrder(any(), any());
    }

    // 3. MAPOWANIE BŁĘDÓW / DLQ (Przepuszczanie wyjątków wyżej dla RabbitMQ)
    @Test
    void shouldBubbleUpExceptionWhenDatabaseIsDown() {
        // Arrange
        OrderActivatedEvent validEvent = new OrderActivatedEvent(
                "EVT-1003", "ORD-777", List.of("WINTER_PACK")
        );

        // Symulujemy awarię bazy danych (np. przerwane połączenie z PostgreSQL)
        doThrow(new RuntimeException("Brak połączenia z bazą danych"))
                .when(inventoryAppService).allocateVehicleForOrder(any(), any());

        // Act & Assert
        // Wyjątek musi wylecieć z metody, żeby RabbitMQ wiedział, że ma wrzucić wiadomość do DLQ (Dead Letter Queue)
        assertThatThrownBy(() -> adapter.handleOrderPlacedEvent(validEvent))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Brak połączenia z bazą danych");
    }
}