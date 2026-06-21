package salon.financing.application.port.out;

/**
 * OUTBOUND PORT (driven, ACL, Figure 42 — BankIntegrationAcl) — integration with the bank system.
 *
 * The Anti-Corruption Layer isolates the Financing core from the bank API formats. The bank is
 * the sole credit decision-maker; submitting the application is asynchronous, and the decision returns later
 * as a separate event (UC-FIN-02).
 */
public interface BankIntegrationAcl {

    /** UC-FIN-01: sending the (translated) financing application to the bank system. */
    void submitFinancingApplication(String orderId);

    /**
     * UC-CRM-03 -> UC-FIN-01: triggering the creditworthiness check for an order
     * (a semantic alias used by the Sales Context). By default it delegates to submitting the application.
     */
    default void startCreditCheckProcess(String orderId) {
        submitFinancingApplication(orderId);
    }
}
