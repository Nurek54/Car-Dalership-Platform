package com.salon.billing.infrastructure.messaging;

import main.java.com.salon.billing.application.port.in.CalculateSettlementCommand;
import main.java.com.salon.billing.application.port.in.CalculateSettlementUseCase;
import main.java.com.salon.billing.application.port.out.SettlementRepository;
import main.java.com.salon.billing.application.service.SettlementAppService;
import main.java.com.salon.billing.domain.model.settlement.OrderSettlement;
import main.java.com.salon.billing.domain.model.settlement.SettlementId;
import main.java.com.salon.billing.domain.model.settlement.SettlementState;
import main.java.com.salon.billing.domain.service.SettlementCalculationService;
import main.java.com.salon.billing.infrastructure.messaging.OrderReadyForSettlementEvent;
import main.java.com.salon.billing.infrastructure.messaging.SettlementEventListener;
import main.java.com.salon.billing.infrastructure.mock.ExternalIntegrationMockAdapter;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Test: UC-ROZ-03 wyzwalany zdarzeniem przez SettlementEventListener.
 */
class SettlementEventListenerTest {

    // Prosty "szpieg" use case'a — zapisuje ostatnią komendę i liczbę wywołań.
    private static class RecordingUseCase implements CalculateSettlementUseCase {
        private int callCount = 0;
        private CalculateSettlementCommand lastCommand = null;

        @Override
        public void calculateSettlement(CalculateSettlementCommand command) {
            this.callCount = this.callCount + 1;
            this.lastCommand = command;
        }
    }

    // Repozytorium łapiące ostatni zapisany agregat (do asercji w teście integracyjnym).
    private static class CapturingSettlementRepository implements SettlementRepository {
        private OrderSettlement lastSaved = null;

        @Override
        public void save(OrderSettlement settlement) {
            this.lastSaved = settlement;
        }

        @Override
        public Optional<OrderSettlement> findById(SettlementId id) {
            return Optional.ofNullable(this.lastSaved);
        }
    }

    private OrderReadyForSettlementEvent sampleEvent(UUID eventId) {
        return new OrderReadyForSettlementEvent(
                eventId,
                "ORDER-1",
                new BigDecimal("100000"),
                new BigDecimal("20000"),
                "PLN",
                Instant.now());
    }

    @Test
    @DisplayName("Event triggers the use case with correctly mapped data")
    void firesUseCaseWithMappedData() {
        RecordingUseCase useCase = new RecordingUseCase();
        SettlementEventListener listener = new SettlementEventListener(useCase);

        listener.on(sampleEvent(UUID.randomUUID()));

        assertEquals(1, useCase.callCount);
        assertNotNull(useCase.lastCommand);
        assertEquals("ORDER-1", useCase.lastCommand.orderId());
        assertEquals("PLN", useCase.lastCommand.currency());
        // BigDecimal porównujemy przez compareTo (ignoruje różnice skali np. 100000 vs 100000.00).
        assertEquals(0, new BigDecimal("100000").compareTo(useCase.lastCommand.vehicleValue()));
        assertEquals(0, new BigDecimal("20000").compareTo(useCase.lastCommand.totalDeposits()));
    }

    @Test
    @DisplayName("Duplicate event (same eventId) is processed only once")
    void ignoresDuplicateEvent() {
        RecordingUseCase useCase = new RecordingUseCase();
        SettlementEventListener listener = new SettlementEventListener(useCase);

        UUID eventId = UUID.randomUUID();
        listener.on(sampleEvent(eventId));
        listener.on(sampleEvent(eventId)); // duplikat — ten sam eventId

        assertEquals(1, useCase.callCount);
    }

    @Test
    @DisplayName("End-to-end: event drives UC-ROZ-03 and settles the order")
    void endToEndSettlesOrder() {
        CapturingSettlementRepository repository = new CapturingSettlementRepository();
        SettlementAppService appService = new SettlementAppService(
                repository,
                new ExternalIntegrationMockAdapter(),
                new SettlementCalculationService());
        SettlementEventListener listener = new SettlementEventListener(appService);

        listener.on(sampleEvent(UUID.randomUUID()));

        assertNotNull(repository.lastSaved);
        // 100000 - 20000 (zadatki) - 50000 (finansowanie mock) - 0 (odkup mock) = 30000
        assertEquals(0, new BigDecimal("30000").compareTo(repository.lastSaved.getFinalBalance().amount()));
        assertEquals(SettlementState.SETTLED, repository.lastSaved.getState());
    }
}
