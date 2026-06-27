package unit.financing_context.events;

import org.junit.jupiter.api.Test;
import salon.financing.application.domain.event.FinancingApplicationFailedEvent;
import salon.financing.application.domain.event.FinancingApprovedEvent;
import salon.financing.application.domain.event.FinancingRejectedEvent;

import static org.assertj.core.api.Assertions.*;

/** Zdarzenia dziedzinowe kontekstu Finansowania (payload + automatyczne metadane). */
class FinancingDomainEventsTest {

    @Test
    void approvedShouldCarryOrderId() {
        // UC-FIN-02: kontekst emituje zdarzenie FinancingApproved
        FinancingApprovedEvent event = new FinancingApprovedEvent("ORD-1");

        assertThat(event.orderId()).isEqualTo("ORD-1");
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredOn()).isNotNull();
    }

    @Test
    void rejectedShouldCarryOrderId() {
        // UC-FIN-02 / A1: kontekst emituje zdarzenie FinancingRejected
        FinancingRejectedEvent event = new FinancingRejectedEvent("ORD-2");

        assertThat(event.orderId()).isEqualTo("ORD-2");
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredOn()).isNotNull();
    }

    @Test
    void applicationFailedShouldCarryOrderAndReason() {
        // UC-FIN-01 / A2: kontekst emituje zdarzenie FinancingApplicationFailed z powodem
        FinancingApplicationFailedEvent event = new FinancingApplicationFailedEvent("ORD-3", "Błędny NIP");

        assertThat(event.orderId()).isEqualTo("ORD-3");
        assertThat(event.reason()).isEqualTo("Błędny NIP");
        assertThat(event.eventId()).isNotNull();
        assertThat(event.occurredOn()).isNotNull();
    }
}
