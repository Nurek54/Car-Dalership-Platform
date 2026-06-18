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
 * Adapter sterujący (driving) — REST API ofert Sprzedaży i CRM.
 *
 * UC-CRM-03: akceptacja oferty przez klienta i utworzenie zamówienia.
 * Wyjątki domenowe mapowane są na kody HTTP (404 — brak oferty, 409 — konflikt stanu).
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

    /** UC-CRM-03, krok 1-3: klient akceptuje ofertę — powstaje zamówienie. */
    @PostMapping("/{offerId}/accept")
    public ResponseEntity<Map<String, String>> acceptOffer(@PathVariable String offerId) {
        String orderId = salesAppService.acceptOfferAndCreateOrder(new OfferId(offerId));
        return ResponseEntity.ok(Map.of("orderId", orderId == null ? "" : orderId));
    }

    /** UC-CRM-03, A1: klient odrzuca ofertę. */
    @PostMapping("/{offerId}/reject")
    public ResponseEntity<Void> rejectOffer(@PathVariable String offerId) {
        salesAppService.rejectOffer(new OfferId(offerId));
        return ResponseEntity.ok().build();
    }

    // Brak oferty -> 404 Not Found z czytelnym komunikatem dla front-endu.
    @ExceptionHandler(OfferNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(OfferNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("message", "Offer " + ex.getOfferId() + " not found in the system"));
    }

    // Akceptacja wygasłej oferty -> 409 Conflict (konflikt ze stanem zasobu).
    @ExceptionHandler(OfferExpiredException.class)
    public ResponseEntity<Map<String, String>> handleExpired(OfferExpiredException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("message", ex.getMessage()));
    }
}
