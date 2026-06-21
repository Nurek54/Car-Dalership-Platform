package salon.sales.infrastructure.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salon.sales.application.service.SalesService;
import salon.sales.application.domain.exception.OfferExpiredException;
import salon.sales.application.domain.exception.OfferNotFoundException;
import salon.sales.application.domain.model.offer.OfferId;

import java.util.Map;

/**
 * Driving adapter — REST API for Sales and CRM offers.
 *
 * UC-CRM-03: the customer's acceptance of the offer and order creation.
 * Domain exceptions are mapped to HTTP codes (404 — no offer, 409 — state conflict).
 */
@RestController
@RequestMapping("/api/sales/offers")
public class OfferRestApiAdapter {

    private final SalesService salesAppService;

    public OfferRestApiAdapter(SalesService salesAppService) {
        if (salesAppService == null) {
            throw new IllegalArgumentException("salesAppService must not be null.");
        }
        this.salesAppService = salesAppService;
    }

    /** UC-CRM-03, steps 1-3: the customer accepts the offer — an order is created. */
    @PostMapping("/{offerId}/accept")
    public ResponseEntity<Map<String, String>> acceptOffer(@PathVariable String offerId) {
        String orderId = salesAppService.acceptOfferAndCreateOrder(new OfferId(offerId));
        return ResponseEntity.ok(Map.of("orderId", orderId == null ? "" : orderId));
    }

    /** UC-CRM-03, A1: the customer rejects the offer. */
    @PostMapping("/{offerId}/reject")
    public ResponseEntity<Void> rejectOffer(@PathVariable String offerId) {
        salesAppService.rejectOffer(new OfferId(offerId));
        return ResponseEntity.ok().build();
    }

    // No offer -> 404 Not Found with a readable message for the front-end.
    @ExceptionHandler(OfferNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(OfferNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "Offer " + ex.offerId() + " not found in the system"));
    }

    // Accepting an expired offer -> 409 Conflict (conflict with the resource state).
    @ExceptionHandler(OfferExpiredException.class)
    public ResponseEntity<Map<String, String>> handleExpired(OfferExpiredException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
    }
}
