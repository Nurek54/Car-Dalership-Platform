package salon.financing.infrastructure.in.messaging;

import salon.financing.application.port.in.ProcessFinancing;

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

    public record FinancingRequested(String orderId, String customerId) {
    }

    public record FinancingDecisionReceivedFromBank(String orderId, boolean approved) {
    }
}
