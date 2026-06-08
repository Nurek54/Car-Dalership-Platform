package integration.driving_adapetrs.event_subscriber_adapter.billing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.sales.application.service.OrderAppService;
import salon.billing.domain.event.DepositRegisteredEvent;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BillingEventSubscriberAdapterTest {

    // Kaskader dla usługi aplikacji w module Sprzedaży
    @Mock
    private OrderAppService orderAppService;

    // Testowany adapter
    @InjectMocks
    private BillingEventSubscriberAdapter adapter;

    // 1. HAPPY PATH: Pomyślne odebranie zadatku i aktywacja zamówienia
    @Test
    void shouldActivateOrderWhenDepositRegisteredEventIsReceived() {
        // Arrange
        DepositRegisteredEvent validEvent = new DepositRegisteredEvent(
                "EVT-3001",
                "ORD-555",
                new BigDecimal("5000.00") // Kwota zadatku
        );

        // Act
        adapter.handleDepositRegisteredEvent(validEvent);

        // Assert - Upewniamy się, że Sprzedaż aktywuje konkretne zamówienie
        verify(orderAppService, times(1)).activateOrder("ORD-555");
    }

    // 2. WALIDACJA WEJŚCIA: Odrzucenie wadliwego zdarzenia (np. z ujemną kwotą)
    @Test
    void shouldRejectEventWhenDepositAmountIsNegative() {
        // Arrange
        DepositRegisteredEvent invalidEvent = new DepositRegisteredEvent(
                "EVT-3002",
                "ORD-555",
                new BigDecimal("-100.00") // BŁĄD: Ujemna kwota!
        );

        // Act & Assert
        assertThatThrownBy(() -> adapter.handleDepositRegisteredEvent(invalidEvent))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Kwota zadatku nie może być ujemna");

        // Gwarancja, że nie aktywowaliśmy zamówienia na podstawie błędnych danych z kolejki
        verify(orderAppService, never()).activateOrder(any());
    }

    // 3. MAPOWANIE BŁĘDÓW / DLQ: Zamówienie nie istnieje w module Sprzedaży
    @Test
    void shouldBubbleUpExceptionWhenOrderNotFoundInSalesModule() {
        // Arrange
        DepositRegisteredEvent validEvent = new DepositRegisteredEvent(
                "EVT-3003",
                "ORD-999", // Zamówienie, którego nie ma w bazie Sprzedaży
                new BigDecimal("5000.00")
        );

        // Symulacja błędu biznesowego z warstwy aplikacji
        doThrow(new IllegalStateException("Nie znaleziono zamówienia ORD-999 do aktywacji"))
                .when(orderAppService).activateOrder("ORD-999");

        // Act & Assert
        // Wyjątek wylatuje na zewnątrz, RabbitMQ łapie go i przenosi wiadomość do Dead Letter Queue (DLQ),
        // żeby administrator mógł sprawdzić, dlaczego ktoś wpłacił zadatek na nieistniejące zamówienie!
        assertThatThrownBy(() -> adapter.handleDepositRegisteredEvent(validEvent))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Nie znaleziono zamówienia ORD-999 do aktywacji");
    }
}