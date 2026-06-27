package unit.financing_context.aggregateTests;

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

/** UC-FIN-01/02: Agregat FinancingApplication (maszyna stanów wniosku o finansowanie). */
class FinancingApplicationTest {

    private final FinancingApplicationFactory factory = new FinancingApplicationFactory();

    private FinancingApplication draft(String orderId) {
        return factory.createDraft(new OrderId(orderId), new CustomerId("CUST-1"),
                new BuyerDetails("Jan Kowalski", "1234563218"), Money.of(100000, "PLN"));
    }

    @Test
    void shouldSubmitDraftApplicationToBank() {
        FinancingApplication application = draft("ORD-1");

        // UC-FIN-01: wysłanie wniosku przenosi go w stan "W trakcie weryfikacji bankowej" (PENDING)
        application.submitApplication();

        assertThat(application.state()).isEqualTo(ApplicationState.PENDING);
    }

    @Test
    void shouldApprovePendingApplication() {
        FinancingApplication application = draft("ORD-2");
        application.submitApplication();

        // UC-FIN-02: pozytywna decyzja banku zatwierdza wniosek
        application.approve();

        assertThat(application.state()).isEqualTo(ApplicationState.APPROVED);
    }

    @Test
    void shouldRejectPendingApplication() {
        FinancingApplication application = draft("ORD-3");
        application.submitApplication();

        // UC-FIN-02 / A1: decyzja odmowna odrzuca wniosek
        application.reject();

        assertThat(application.state()).isEqualTo(ApplicationState.REJECTED);
    }

    @Test
    void shouldRejectSubmissionWhenNotDraft() {
        FinancingApplication application = draft("ORD-4");
        application.submitApplication(); // już PENDING

        // Tylko wniosek w stanie DRAFT może zostać wysłany
        assertThatThrownBy(application::submitApplication)
                .isInstanceOf(IllegalApplicationStateException.class);
    }

    @Test
    void shouldRejectDecisionWhenNotPending() {
        FinancingApplication application = draft("ORD-5"); // wciąż DRAFT

        // Decyzję można przetworzyć tylko dla wniosku oczekującego (PENDING)
        assertThatThrownBy(application::approve)
                .isInstanceOf(IllegalApplicationStateException.class);
        assertThatThrownBy(application::reject)
                .isInstanceOf(IllegalApplicationStateException.class);
    }
}
