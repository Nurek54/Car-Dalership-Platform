package salon.financing.application.port.out;

/**
 * Port wyjściowy (ACL): wystawienie polisy u zewnętrznego ubezpieczyciela (UC-FIN-02).
 * Zwraca numer/referencję polisy nadaną przez ubezpieczyciela.
 */
public interface InsurerIntegrationAclPort {
    String issuePolicy(String vin, java.math.BigDecimal insuredAmount);
}
