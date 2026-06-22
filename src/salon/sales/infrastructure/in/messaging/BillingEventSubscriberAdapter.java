package salon.sales.infrastructure.in.messaging;

import salon.sales.application.port.in.ActivateOrderOnDeposit;

/**
 * INBOUND ADAPTER (Figure 22: EventListener) — subscriber of the Billing and Settlement Context.
 *
 * On AdvancePaymentRegistered the order is activated (UC-CRM-03, part 2). ACL: the Billing message
 * is translated into a simple local record; the adapter drives the {@link ActivateOrderOnDeposit} port.
 */
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

    /** Local (ACL) representation of the AdvancePaymentRegistered event from Billing. */
    public record AdvancePaymentRegistered(String orderId) {
    }
}
