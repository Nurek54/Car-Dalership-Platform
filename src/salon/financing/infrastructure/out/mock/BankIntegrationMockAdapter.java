package salon.financing.infrastructure.out.mock;

import salon.financing.application.port.out.BankIntegrationAcl;

/**
 * Udawany ACL banku — zawsze zwraca pozytywną decyzję (do testów/dem).
 */
public class BankIntegrationMockAdapter implements BankIntegrationAcl {

    @Override
    public void submitApplication(String applicationId, String customerId) {
        // no-op (symulacja wysłania wniosku)
    }

    @Override
    public boolean isApproved(String applicationId) {
        return true;
    }
}
