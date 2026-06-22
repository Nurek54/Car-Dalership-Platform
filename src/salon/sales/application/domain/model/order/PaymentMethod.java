package salon.sales.application.domain.model.order;

/**
 * Enumeration (Figure 23) — the payment method declared by the customer on the order (UC-CRM-03).
 *
 * BANK_TRANSFER – emits BankTransferDeclaredEvent; FINANCING – emits FinancingRequestedEvent.
 */
public enum PaymentMethod {
    BANK_TRANSFER,
    FINANCING
}
