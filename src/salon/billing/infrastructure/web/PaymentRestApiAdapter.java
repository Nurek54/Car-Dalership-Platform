package salon.billing.infrastructure.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salon.billing.application.port.in.ProcessPaymentCommand;
import salon.billing.application.service.SettlementAppService;
import salon.sales.domain.exceptions.OfferExpiredException;

import java.util.Map;

/**
 * Adapter sterujący (driving) — REST API księgowania wpłat (UC-FIR-03).
 * Cienki: waliduje wejście, buduje komendę i deleguje do warstwy aplikacji.
 */
@RestController
@RequestMapping("/api/billing/payments")
public class PaymentRestApiAdapter {

    private final SettlementAppService settlementAppService;

    public PaymentRestApiAdapter(SettlementAppService settlementAppService) {
        if (settlementAppService == null) {
            throw new IllegalArgumentException("settlementAppService must not be null.");
        }
        this.settlementAppService = settlementAppService;
    }

    @PostMapping
    public ResponseEntity<Void> registerPayment(@Valid @RequestBody PaymentRequest request) {
        ProcessPaymentCommand command = new ProcessPaymentCommand(
                request.orderId(),
                request.transactionId(),
                request.amount(),
                request.currency(),
                null);
        settlementAppService.processPayment(command);
        return ResponseEntity.ok().build();
    }

    // Wpłata do nieistniejącego zamówienia -> 404 Not Found (zasób nieodnaleziony).
    @ExceptionHandler(OfferExpiredException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(OfferExpiredException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", ex.getMessage()));
    }
}
