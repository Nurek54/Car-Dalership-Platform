package salon.financing.application.port.out;

import salon.common.model.Money;
import salon.financing.application.domain.model.financing.BuyerDetails;

/**
 * OUTBOUND PORT (Figure 42 — SalesIntegration, ACL) — synchronous queries (Query)
 * the Sales and CRM Context for the data needed for the financing application (UC-FIN-01):
 * the buyer data and the final price of the order's source offer.
 *
 * The contract is expressed in the Financing-local type (BuyerDetails) and the Shared Kernel (Money);
 * the order identified by a string (a disjoint model), translated in the adapter (ACL).
 */
public interface SalesIntegration {

    /** Buyer data for the order (for the bank's creditworthiness assessment). */
    BuyerDetails buyerDetails(String orderId);

    /** Final price of the order's source offer — the amount of financing requested. */
    Money offerFinalPrice(String orderId);
}
