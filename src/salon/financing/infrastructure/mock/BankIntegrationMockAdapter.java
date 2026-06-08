package salon.financing.infrastructure.mock;

import salon.financing.application.port.out.BankIntegrationAclPort;
import salon.financing.domain.model.financing.DecisionStatus;
import salon.financing.domain.model.financing.FinancingDecision;

import java.math.BigDecimal;

/**
 * Udawany ACL banku — zawsze zwraca pozytywną decyzję (do testów/dem).
 */
public class BankIntegrationMockAdapter implements BankIntegrationAclPort {

    @Override
    public void submitApplication(String applicationId, String customerId, BigDecimal amount) {
        // no-op (symulacja wysłania)
    }

    @Override
    public FinancingDecision fetchDecision(String applicationId) {
        return new FinancingDecision("BANK-REF-" + applicationId, new BigDecimal("100000.00"), DecisionStatus.APPROVED);
    }
}
