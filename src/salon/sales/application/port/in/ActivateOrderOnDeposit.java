package salon.sales.application.port.in;

/**
 * Inbound port triggered by an event from Billing (PaymentRegisteredEvent / PaymentPosted).
 * This implements step 5 of UC-SPR-02: posting the deposit unblocks the order fulfillment.
 */
public interface ActivateOrderOnDeposit {
    void activateOnDeposit(String orderId);
}
