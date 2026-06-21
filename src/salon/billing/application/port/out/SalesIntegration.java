package salon.billing.application.port.out;

import salon.billing.application.domain.model.document.BuyerDetails;

/**
 * OUTBOUND PORT (Fig. 48 — SalesIntegration, ACL) — a synchronous query (Query) to the Context
 * Sales and CRM for the buyer data needed on the invoice (UC-FIR-01/02).
 *
 * The triggering event carries only orderId (GDPR compliance) — this port fetches the buyer data by
 * orderId. The contract is expressed in the Billing-local type (BuyerDetails), translated from the snapshot
 * of Sales through an adapter (Anti-Corruption Layer).
 */
public interface SalesIntegration {

    BuyerDetails buyerDetailsFor(String orderId);
}
