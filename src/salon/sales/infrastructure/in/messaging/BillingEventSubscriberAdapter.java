package salon.sales.infrastructure.in.messaging;

import salon.sales.application.port.in.ActivateOrderOnDeposit;

public class BillingEventSubscriberAdapter {

    private final ActivateOrderOnDeposit activateOrderOnDeposit;

    public BillingEventSubscriberAdapter(ActivateOrderOnDeposit activateOrderOnDeposit) {
        if (activateOrderOnDeposit == null) {
            throw new IllegalArgumentException("activateOrderOnDeposit must not be null.");
        }
        this.activateOrderOnDeposit = activateOrderOnDeposit;
    }

    public void handleAdvancePaymentRegistered(AdvancePaymentRegistered event) {
        if (event == null || event.orderId() == null || event.orderId().isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        this.activateOrderOnDeposit.activateOnDeposit(event.orderId());
    }

    public record AdvancePaymentRegistered(String orderId) {
    }
}
