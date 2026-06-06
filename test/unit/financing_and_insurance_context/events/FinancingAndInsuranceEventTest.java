package unit.financing_and_insurance_context.events;

import org.junit.jupiter.api.Test;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

class FinancingAndInsuranceEventTest {

    @Test
    void shouldEmitFinancingApprovedEventWhenBankAccepts() {
        // Arrange
        FinancingApplication app = new FinancingApplication(
                new ApplicationId("APP-1"), new OrderId("ORD-1"), new CustomerId("C-1"), Money.of(100000, "PLN")
        );
        app.submitApplication();

        FinancingDecision positiveDecision = new FinancingDecision("BANK-123", new BigDecimal("100000.00"), DecisionStatus.APPROVED);

        // Act
        app.processBankDecision(positiveDecision);

        // Assert
        assertThat(app.getDomainEvents())
                .hasAtLeastOneElementOfType(FinancingApprovedEvent.class);
    }

    @Test
    void shouldEmitPolicyActivatedEventWhenGapIsIssued() {
        // Arrange
        InsurancePolicy policy = new InsurancePolicy(new PolicyId("POL-1"), new VinNumber("VIN123"), PolicyType.GAP_INSURANCE);
        policy.assignResidualValue(Money.of(80000, "PLN"));

        // Act
        policy.activatePolicy("INSURER-REF-999");

        // Assert
        assertThat(policy.getDomainEvents())
                .hasAtLeastOneElementOfType(PolicyActivatedEvent.class);
    }
}