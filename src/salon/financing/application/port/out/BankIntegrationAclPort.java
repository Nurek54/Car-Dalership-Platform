package salon.financing.application.port.out;

/**
 * Port wyjściowy (ACL): wysłanie wniosku do banku i odbiór zweryfikowanej decyzji (UC-FIN-01).
 * ACL tłumaczy "brudny" model bankowy na prostą decyzję boolowską dla domeny.
 */
public interface BankIntegrationAclPort {
    void submitApplication(String applicationId, String customerId);
    boolean isApproved(String applicationId);
}
