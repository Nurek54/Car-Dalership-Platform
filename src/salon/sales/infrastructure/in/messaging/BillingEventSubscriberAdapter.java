package salon.sales.infrastructure.in.messaging;

import salon.billing.application.domain.event.AdvancePaymentRequestedEvent;
import salon.billing.application.domain.event.InvoiceCreatedEvent;
import salon.billing.application.domain.event.PaymentRegisteredEvent;
import salon.sales.application.port.in.ActivateOrderOnDeposit;

/**
 * Driving adapter — subscriber of the Billing and Settlement Context events
 * in the Sales Context (communication per the canvas: AdvancePaymentRequested, InvoiceCreated,
 * PaymentRegistered flow to Sales and CRM).
 *
 * PaymentRegistered (the first posted payment/deposit) activates the order
 * (UC-CRM-03 part 2, Fig. 19/20 PDF). The remaining events serve to inform the Salesperson
 * about the settlement progress.
 */
public class BillingEventSubscriberAdapter {

    private final ActivateOrderOnDeposit activateOrder;

    public BillingEventSubscriberAdapter(ActivateOrderOnDeposit activateOrder) {
        if (activateOrder == null) {
            throw new IllegalArgumentException("activateOrder must not be null.");
        }
        this.activateOrder = activateOrder;
    }

    /** A customer payment was posted — order activation (idempotent on the service side). */
    public void handlePaymentRegistered(PaymentRegisteredEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        if (event.orderId() == null || event.orderId().isBlank()) {
            throw new IllegalArgumentException("The order identifier (orderId) is required");
        }
        activateOrder.activateOnDeposit(event.orderId());
    }

    /** Billing asked the customer for a deposit — information for the Salesperson (no state change). */
    public void handleAdvancePaymentRequested(AdvancePaymentRequestedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        System.out.println("[BillingEventSubscriberAdapter] The customer of order " + event.orderId()
                + " was asked to pay the deposit.");
    }

    /** The final invoice was issued — information for the Salesperson (no state change). */
    public void handleInvoiceCreated(InvoiceCreatedEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("event must not be null.");
        }
        System.out.println("[BillingEventSubscriberAdapter] An invoice was issued for order "
                + event.orderId() + ".");
    }
}
