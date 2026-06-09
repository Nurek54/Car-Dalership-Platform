package unit.financing_and_insurance_context;

import org.junit.jupiter.api.Test;
import salon.financing.domain.model.financing.ApplicationId;
import salon.financing.domain.model.financing.ApplicationState;
import salon.financing.domain.model.financing.CustomerId;
import salon.financing.domain.model.financing.FinancingApplication;
import salon.shared.model.OrderId;

import static org.assertj.core.api.Assertions.*;

class FinancingApplicationTest {

    private FinancingApplication newApplication(String appId) {
        return new FinancingApplication(
                new ApplicationId(appId),
                new OrderId("ORD-123"),
                new CustomerId("CUST-999"));
    }

    @Test
    void shouldTransitionToPendingWhenApplicationIsSubmitted() {
        FinancingApplication application = newApplication("APP-001");

        application.submitApplication();

        // Wysłanie wniosku do banku zmienia jego stan na PENDING
        assertThat(application.getState()).isEqualTo(ApplicationState.PENDING);
    }

    @Test
    void shouldApprovePendingApplication() {
        FinancingApplication application = newApplication("APP-002");
        application.submitApplication();

        application.approve();

        assertThat(application.getState()).isEqualTo(ApplicationState.APPROVED);
    }

    @Test
    void shouldRejectPendingApplication() {
        FinancingApplication application = newApplication("APP-003");
        application.submitApplication();

        application.reject();

        assertThat(application.getState()).isEqualTo(ApplicationState.REJECTED);
    }

    @Test
    void shouldNotApproveBeforeSubmission() {
        FinancingApplication application = newApplication("APP-004");

        // Wniosek w stanie DRAFT nie może zostać zatwierdzony
        assertThatThrownBy(application::approve)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Only a PENDING application can be approved");
    }
}
