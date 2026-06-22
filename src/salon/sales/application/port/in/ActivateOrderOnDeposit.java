package salon.sales.application.port.in;

/**
 * INBOUND PORT (Figure 22) — "ActivateOrderOnDeposit".
 * UC-CRM-03 (part 2): activate the order once the deposit payment has been registered
 * (trigger: a payment event from the Billing Context).
 */
public interface ActivateOrderOnDeposit {

    void activateOnDeposit(String orderId);
}
