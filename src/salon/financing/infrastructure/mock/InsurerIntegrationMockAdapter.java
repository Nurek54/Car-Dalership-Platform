package salon.financing.infrastructure.mock;

import salon.financing.application.port.out.InsurerIntegrationAclPort;

import java.math.BigDecimal;

/**
 * Udawany ACL ubezpieczyciela — zwraca sztuczny numer polisy.
 */
public class InsurerIntegrationMockAdapter implements InsurerIntegrationAclPort {

    @Override
    public String issuePolicy(String vin, BigDecimal insuredAmount) {
        return "POLICY-REF-" + vin;
    }
}
