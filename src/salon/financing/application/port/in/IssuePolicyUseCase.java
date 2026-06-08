package salon.financing.application.port.in;

/**
 * Port wejściowy: wystawienie polisy (UC-FIN-02).
 */
public interface IssuePolicyUseCase {
    String issuePolicy(String vin, String policyType, java.math.BigDecimal basePrice, String currency, int ageInYears);
}
