package salon.financing.application.port.out;

import salon.financing.domain.model.financing.FinancingDecision;

/**
 * Port wyjściowy (ACL): wysłanie wniosku do banku i odbiór zweryfikowanej decyzji (UC-FIN-01).
 * Tłumaczy "brudny" model bankowy na czysty Obiekt Wartości FinancingDecision.
 */
public interface BankIntegrationAclPort {
    void submitApplication(String applicationId, String customerId, java.math.BigDecimal amount);
    FinancingDecision fetchDecision(String applicationId);
}
