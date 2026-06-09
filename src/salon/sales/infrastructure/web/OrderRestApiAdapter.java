package salon.sales.infrastructure.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salon.sales.application.port.in.CreateOrderCommand;
import salon.sales.application.service.OrderAppService;
import salon.sales.domain.exceptions.OfferExpiredException;

import java.net.URI;
import java.util.Map;

/**
 * Adapter sterujący (driving) — REST API zamówień Sprzedaży.
 *
 * UC-SPR-02/UC-CRM-03: tworzenie zamówienia z oferty.
 * UC-CRM-05: rejestracja fizycznego wydania pojazdu ("przycisk potwierdzenia wydania").
 */
@RestController
@RequestMapping("/api/sales/orders")
public class OrderRestApiAdapter {

    private final OrderAppService orderAppService;

    public OrderRestApiAdapter(OrderAppService orderAppService) {
        if (orderAppService == null) {
            throw new IllegalArgumentException("orderAppService must not be null.");
        }
        this.orderAppService = orderAppService;
    }

    @PostMapping
    public ResponseEntity<Void> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        String orderId = orderAppService.createOrderFromOffer(
                new CreateOrderCommand(request.offerId(), request.customerSignature()));
        return ResponseEntity.created(URI.create("/api/sales/orders/" + orderId)).build();
    }

    /**
     * UC-CRM-05, krok 2-4: Handlowiec potwierdza fizyczne wydanie pojazdu. Zamówienie przechodzi
     * w stan "Zrealizowane" (COMPLETED), a CRM publikuje sygnał zwolnienia pojazdu do Inwentarza.
     */
    @PostMapping("/{orderId}/handover")
    public ResponseEntity<Void> completeHandover(@PathVariable String orderId) {
        orderAppService.completeHandover(orderId);
        return ResponseEntity.noContent().build();
    }

    // Konwersja wygasłej oferty -> 409 Conflict (konflikt ze stanem zasobu).
    @ExceptionHandler(OfferExpiredException.class)
    public ResponseEntity<Map<String, String>> handleConflict(OfferExpiredException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }

    // Nieprawidłowy stan zamówienia (np. odbiór nieumówiony) lub brak zamówienia -> 409 Conflict.
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }
}
