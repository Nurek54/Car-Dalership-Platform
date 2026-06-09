package unit.financing_and_insurance_context.events;

import org.junit.jupiter.api.Test;
import salon.financing.domain.event.FinancingApprovedEvent;
import salon.financing.domain.event.FinancingRejectedEvent;
import salon.financing.domain.model.financing.ApplicationId;
import salon.financing.domain.model.financing.CustomerId;
import salon.financing.domain.model.financing.FinancingApplication;
import salon.shared.model.OrderId;

import static org.assertj.core.api.Assertions.*;

class FinancingAndInsuranceEventTest {

    private FinancingApplication submitted(String appId, String orderId) {
        FinancingApplication app = new FinancingApplication(
                new ApplicationId(appId), new OrderId(orderId), new CustomerId("C-1"));
        app.submitApplication();
        return app;
    }

    @Test
    void shouldEmitFinancingApprovedEventWhenBankAccepts() {
        FinancingApplication app = submitted("APP-1", "ORD-1");

        app.approve();

        assertThat(app.getDomainEvents())
                .hasAtLeastOneElementOfType(FinancingApprovedEvent.class);
    }

    @Test
    void shouldEmitFinancingRejectedEventWhenBankDeclines() {
        FinancingApplication app = submitted("APP-2", "ORD-2");

        app.reject();

        assertThat(app.getDomainEvents())
                .hasAtLeastOneElementOfType(FinancingRejectedEvent.class);
    }
}
