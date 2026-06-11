package salon.sales.application.port.in;

import salon.sales.domain.model.offer.OfferId;

/**
 * Port wejściowy dla UC-CRM-02 (wygenerowanie oferty proforma) — odpowiednik węzła
 * "IssueProformaUseCase" w docs/Architecture/SalesArchitecture.md (PDF rozdz. 3.3.3).
 * Wycena specyfikacji dociągana jest z modułu Katalogu (port wyjściowy).
 */
public interface GenerateOfferUseCase {

    OfferId generateOffer(String customerId, String specificationId);
}
