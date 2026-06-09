package integration.driving_adapetrs.event_subscriber_adapter.billing;

import salon.sales.infrastructure.messaging.BillingEventSubscriberAdapter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.billing.domain.event.AdvancePaymentRequestedEvent;
import salon.sales.application.service.OrderAppService;

import java.time.Instant;
import java.util.UUID;

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

    // 1. HAPPY PATH: Odebranie żądania zadatku i aktywacja zamówienia
    @Test
    void shouldActivateOrderWhenAdvancePaymentRequestedEventIsReceived() {
        AdvancePaymentRequestedEvent validEvent = new AdvancePaymentRequestedEvent(
                UUID.randomUUID(), "SET-3001", "ORD-555", Instant.now());

        adapter.handleAdvancePaymentRequested(validEvent);

        verify(orderAppService, times(1)).activateOrder("ORD-555");
    }

    // 2. MAPOWANIE BŁĘDÓW / DLQ: Zamówienie nie istnieje w module Sprzedaży
    @Test
    void shouldBubbleUpExceptionWhenOrderNotFoundInSalesModule() {
        AdvancePaymentRequestedEvent validEvent = new AdvancePaymentRequestedEvent(
                UUID.randomUUID(), "SET-3003", "ORD-999", Instant.now());

        doThrow(new IllegalStateException("Nie znaleziono zamówienia ORD-999 do aktywacji"))
                .when(orderAppService).activateOrder("ORD-999");

        assertThatThrownBy(() -> adapter.handleAdvancePaymentRequested(validEvent))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Nie znaleziono zamówienia ORD-999 do aktywacji");
    }
}
