package salon.sales.application.domain.model.order;

/** The order's payment status (synchronized from the Billing Context events). */
public enum PaymentStatus {
    UNPAID,   // no posted payments
    PARTIAL,  // a partial payment (e.g. a deposit)
    PAID      // the balance is fully covered (SettlementCompleted)
}
