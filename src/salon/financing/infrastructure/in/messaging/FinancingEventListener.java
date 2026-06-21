package salon.financing.infrastructure.in.messaging;

import salon.financing.application.port.in.ProcessFinancing;

/**
 * INBOUND ADAPTER (Figure 42: EventListener) – subscriber of the Financing Context events.
 *
 * Maps events from the bus to the inbound port {@link ProcessFinancing}:
 *  - FinancingRequested (from CRM)           -> UC-FIN-01 (application submission),
 *  - FinancingDecisionReceivedFromBank (ACL) -> UC-FIN-02 (processing the decision).
 *
 * ACL: we represent external messages as local records and translate them into a port call.
 */
public class FinancingEventListener {

    private final ProcessFinancing processFinancing;

    public FinancingEventListener(ProcessFinancing processFinancing) {
        if (processFinancing == null) {
            throw new IllegalArgumentException("processFinancing must not be null.");
        }
        this.processFinancing = processFinancing;
    }

    public void handleFinancingRequested(FinancingRequested event) {
        if (event == null || event.orderId() == null || event.orderId().isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        this.processFinancing.requestFinancing(event.orderId(), event.customerId());
    }

    public void handleFinancingDecisionReceived(FinancingDecisionReceivedFromBank event) {
        if (event == null || event.orderId() == null || event.orderId().isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        this.processFinancing.processBankDecision(event.orderId(), event.approved());
    }

    /** Local (ACL) representation of the FinancingRequested event from the Sales and CRM Context. */
    public record FinancingRequested(String orderId, String customerId) {
    }

    /** Local (ACL) representation of the asynchronous decision from the bank adapter. */
    public record FinancingDecisionReceivedFromBank(String orderId, boolean approved) {
    }
}
