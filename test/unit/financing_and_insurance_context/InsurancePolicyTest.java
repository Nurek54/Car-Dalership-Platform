package unit.financing_and_insurance_context;

import org.junit.jupiter.api.Test;
import salon.financing.domain.model.insurance.InsurancePolicy;
import salon.financing.domain.model.insurance.PolicyId;
import salon.financing.domain.model.insurance.PolicyState;
import salon.financing.domain.model.insurance.PolicyType;
import salon.financing.domain.model.insurance.VinNumber;
import salon.shared.model.Money;

import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.*;

class InsurancePolicyTest {

    @Test
    void shouldThrowExceptionWhenActivatingPolicyWithoutInsuredValue() {
        // Arrange (Given)
        InsurancePolicy policy = new InsurancePolicy(
                new PolicyId("POL-001"),
                new VinNumber("VIN1234567890ABCDE"),
                PolicyType.GAP_INSURANCE
        );

        // Act & Assert (When & Then)
        // Oczekujemy, że agregat zablokuje aktywację polisy z powodu braku wyliczonej wartości rezydualnej
        assertThatThrownBy(() -> policy.activatePolicy("INSURER-REF-001"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cannot activate policy without assigned residual value");
    }

    @Test
    void shouldSuccessfullyActivatePolicyWhenInsuredValueIsAssigned() {
        // Arrange (Given)
        InsurancePolicy policy = new InsurancePolicy(
                new PolicyId("POL-002"),
                new VinNumber("VIN9876543210EDCBA"),
                PolicyType.GAP_INSURANCE
        );

        // Przypisanie wyliczonej wartości ubezpieczenia (pochodzącej z ResidualValueCalculationService)
        Money residualValue = Money.of(new BigDecimal("80000.00"), "PLN");
        policy.assignResidualValue(residualValue);

        // Act (When)
        policy.activatePolicy("INSURER-REF-002");

        // Assert (Then)
        // Polisa przechodzi w stan ACTIVE
        assertThat(policy.getState()).isEqualTo(PolicyState.ACTIVE);
        assertThat(policy.getInsuredValue()).isEqualTo(residualValue);
    }
}