package salon.sales.application.port.in;

import salon.sales.application.domain.model.offer.OfferId;

/**
 * Inbound port for UC-CRM-02 (generating the proforma offer) — the counterpart of the node
 * "IssueProformaUseCase" w docs/Architecture/SalesArchitecture.md (PDF rozdz. 3.3.3).
 * The specification pricing is fetched from the Catalog module (outbound port).
 */
public interface ReceiveSpecificationUseCase {

    OfferId generateOffer(String customerId, String specificationId);
}
