package salon.sales.infrastructure.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salon.sales.application.domain.model.offer.OfferId;
import salon.sales.application.service.SalesService;

/**
 * INBOUND ADAPTER (Figure 22: RestController) — exposes the offer-acceptance use case over HTTP
 * (UC-CRM-03). Maps POST /api/sales/offers/{id}/accept onto {@link SalesService}.
 */
@RestController
@RequestMapping("/api/sales/offers")
public class OfferRestApiAdapter {

    private final SalesService salesService;

    public OfferRestApiAdapter(SalesService salesService) {
        this.salesService = salesService;
    }

    /** The customer accepts a published offer; an order is created and its id returned. */
    @PostMapping("/{id}/accept")
    public ResponseEntity<String> acceptOffer(@PathVariable("id") String offerId) {
        String orderId = this.salesService.acceptOfferAndCreateOrder(new OfferId(offerId));
        return ResponseEntity.ok(orderId);
    }
}
