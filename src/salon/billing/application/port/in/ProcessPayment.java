package salon.billing.application.port.in;

import salon.billing.application.command.ProcessPaymentCommand;
import salon.common.model.Money;
import salon.common.model.OrderId;

/**
 * INBOUND PORT (Fig. 48 — ProcessPayment) — UC-FIR-03: Processing payments.
 *
 * Main entry point for the order balance: balance initialization (event from the Sales Context),
 * posting a transfer from the statement (accounting REST adapter) and periodic payment reminders.
 */
public interface ProcessPayment {

    /** Initializes the balance for a new order (contract value). */
    void initializeSettlement(OrderId orderId, Money totalAmount);

    /** UC-FIR-03: posting the matched transfer and recomputing the balance. */
    void processPayment(ProcessPaymentCommand command);

    /** Periodic task: reminders about unpaid balances. */
    void sendPaymentReminders();
}
