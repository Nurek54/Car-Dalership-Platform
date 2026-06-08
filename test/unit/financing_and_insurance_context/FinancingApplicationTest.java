package unit.financing_and_insurance_context;

import org.junit.jupiter.api.Test;
import salon.financing.domain.model.financing.ApplicationId;
import salon.financing.domain.model.financing.ApplicationState;
import salon.financing.domain.model.financing.CustomerId;
import salon.financing.domain.model.financing.DecisionStatus;
import salon.financing.domain.model.financing.FinancingApplication;
import salon.financing.domain.model.financing.FinancingDecision;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;


class FinancingApplicationTest {

    @Test
    void shouldTransitionToSubmittedStateWhenApplicationIsSubmitted() {
        // Arrange (Given)
        FinancingApplication application = new FinancingApplication(
                new ApplicationId("APP-001"),
                new OrderId("ORD-123"),
                new CustomerId("CUST-999"),
                Money.of(new BigDecimal("100000.00"), "PLN")
        );

        // Act (When)
        application.submitApplication();

        // Assert (Then)
        // Wysłanie wniosku do banku zmienia jego stan na SUBMITTED_TO_BANK
        assertThat(application.getState()).isEqualTo(ApplicationState.SUBMITTED_TO_BANK);
    }

    @Test
    void shouldApproveApplicationOnlyWhenValidBankDecisionIsProcessed() {
        // Arrange (Given)
        FinancingApplication application = new FinancingApplication(
                new ApplicationId("APP-002"),
                new OrderId("ORD-124"),
                new CustomerId("CUST-888"),
                Money.of(new BigDecimal("50000.00"), "PLN")
        );
        application.submitApplication();

        // Obiekt Wartości pochodzący z warstwy ACL
        FinancingDecision positiveDecision = new FinancingDecision(
                "BANK-REF-123",
                new BigDecimal("50000.00"),
                DecisionStatus.APPROVED
        );

        // Act (When)
        application.processBankDecision(positiveDecision);

        // Assert (Then)
        // System pozwala na zmianę stanu na APPROVED dopiero po przetworzeniu FinancingDecision
        assertThat(application.getState()).isEqualTo(ApplicationState.APPROVED);
        assertThat(application.getBankDecision()).isEqualTo(positiveDecision);
    }
}