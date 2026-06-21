package salon.sales.application.domain.model.order;

/**
 * The payment method for the vehicle declared by the customer (UC-CRM-03, step 4) —
 * per docs/Agregate/Sales/customer-offer-order.md.
 *
 * The declaration determines which event will leave the Sales context:
 * BankTransferDeclaredEvent (transfer) or FinancingRequestedEvent (financing).
 */
public enum PaymentMethod {
    BANK_TRANSFER,  // the customer's own bank transfer
    FINANCING       // credit/leasing through the Financing Context
}
