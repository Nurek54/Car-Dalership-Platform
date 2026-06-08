package integration.driven_adapters.database_adapter.financing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import salon.financing.domain.model.insurance.InsurancePolicy;
import salon.financing.domain.model.insurance.PolicyId;
import salon.financing.domain.model.insurance.PolicyType;
import salon.financing.domain.model.insurance.PolicyState;
import salon.financing.domain.model.insurance.VinNumber;
import salon.shared.model.Money;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(InsurancePolicyDatabaseAdapter.class)
class InsurancePolicyDatabaseAdapterTest {

    @Autowired
    private InsurancePolicyDatabaseAdapter adapter;

    // 1. ZAPIS I MAPOWANIE: Enumy i powiązania z numerem VIN
    @Test
    void shouldSaveAndRetrieveInsurancePolicyWithCorrectEnumMappings() {
        // Arrange
        PolicyId policyId = new PolicyId("POL-GAP-888");
        VinNumber vin = new VinNumber("VIN1234567890ABCDE");

        InsurancePolicy policy = new InsurancePolicy(policyId, vin, PolicyType.GAP_INSURANCE);
        policy.assignResidualValue(Money.of(new BigDecimal("90000.00"), "PLN"));

        // Aktywujemy polisę, przekazując numer od ubezpieczyciela
        policy.activatePolicy("EXTERNAL-INSURER-REF-1");

        // Act
        adapter.save(policy);

        Optional<InsurancePolicy> retrievedPolicy = adapter.findById(policyId);

        // Assert
        assertThat(retrievedPolicy).isPresent();
        InsurancePolicy retrieved = retrievedPolicy.get();

        assertThat(retrieved.getId()).isEqualTo(policyId);
        assertThat(retrieved.getVinNumber()).isEqualTo(vin);

        // Weryfikacja czy Enumy zmapowały się poprawnie (częsty błąd z @Enumerated(EnumType.ORDINAL))
        assertThat(retrieved.getType()).isEqualTo(PolicyType.GAP_INSURANCE);
        assertThat(retrieved.getState()).isEqualTo(PolicyState.ACTIVE);

        // Sprawdzenie, czy referencja zewnętrzna przetrwała zapis
        assertThat(retrieved.getExternalReference()).isEqualTo("EXTERNAL-INSURER-REF-1");
    }

    // 2. BRAK DANYCH
    @Test
    void shouldReturnEmptyOptionalWhenInsurancePolicyDoesNotExist() {
        // Act
        Optional<InsurancePolicy> result = adapter.findById(new PolicyId("POL-UNKNOWN"));

        // Assert
        assertThat(result).isEmpty();
    }
}