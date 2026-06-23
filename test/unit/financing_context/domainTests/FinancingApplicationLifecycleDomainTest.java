package unit.financing_context.domainTests;

import org.junit.jupiter.api.Test;
import salon.common.model.Money;
import salon.financing.application.domain.exception.IllegalApplicationStateException;
import salon.financing.application.domain.model.financing.ApplicationState;
import salon.financing.application.domain.model.financing.BuyerDetails;
import salon.financing.application.domain.model.financing.CustomerId;
import salon.financing.application.domain.model.financing.FinancingApplication;
import salon.financing.application.domain.model.financing.FinancingApplicationFactory;
import salon.financing.application.domain.model.financing.OrderId;

import static org.assertj.core.api.Assertions.*;

/** UC-FIN-01/02: Pełny cykl życia wniosku o finansowanie (na agregacie). */
class FinancingApplicationLifecycleDomainTest {

    private final FinancingApplicationFactory factory = new FinancingApplicationFactory();

    private FinancingApplication draft(String orderId) {
        return factory.createDraft(new OrderId(orderId), new CustomerId("CUST-1"),
                new BuyerDetails("Jan Kowalski", "1234563218"), Money.of(100000, "PLN"));
    }

    @Test
    void approvalRouteShouldEndApproved() {
        // Ścieżka pozytywna: DRAFT -> PENDING -> APPROVED
        FinancingApplication application = draft("ORD-1");
        assertThat(application.state()).isEqualTo(ApplicationState.DRAFT);

        application.submitApplication();
        assertThat(application.state()).isEqualTo(ApplicationState.PENDING);

        application.approve();
        assertThat(application.state()).isEqualTo(ApplicationState.APPROVED);
    }

    @Test
    void rejectionRouteShouldEndRejected() {
        // Ścieżka negatywna: DRAFT -> PENDING -> REJECTED
        FinancingApplication application = draft("ORD-2");
        application.submitApplication();
        application.reject();

        assertThat(application.state()).isEqualTo(ApplicationState.REJECTED);
    }

    @Test
    void shouldNotProcessDecisionBeforeSubmission() {
        // Decyzji nie można przetworzyć dla wniosku, który nie został wysłany do banku
        FinancingApplication application = draft("ORD-3");

        assertThatThrownBy(application::approve)
                .isInstanceOf(IllegalApplicationStateException.class);
    }
}
