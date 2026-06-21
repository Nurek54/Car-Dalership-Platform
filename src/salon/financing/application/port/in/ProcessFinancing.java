package salon.financing.application.port.in;

/**
 * INBOUND PORT (Figure 42 — ProcessFinancing) — the only inbound port of the Financing Context.
 *
 * Realizes the whole process (per the diagram — one port, not a separate decision port):
 *  - UC-FIN-01 (requestFinancing) — submitting the financing application (trigger: FinancingRequested from CRM),
 *  - UC-FIN-02 (processBankDecision) — processing the bank's asynchronous decision.
 *
 * The dealership does not make the credit decision — it only reflects the status assigned by the bank.
 */
public interface ProcessFinancing {

    /** UC-FIN-01: submitting a financing application for an order. */
    void requestFinancing(String orderId, String customerId);

    /** UC-FIN-02: registering the bank's decision (positive/negative) for an order. */
    void processBankDecision(String orderId, boolean approved);
}
