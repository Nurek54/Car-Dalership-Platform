package salon.sales.infrastructure.in.web;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salon.sales.application.command.ScheduleHandoverCommand;
import salon.sales.application.service.SalesService;
import salon.sales.application.domain.exception.OrderNotFoundException;
import salon.common.model.OrderId;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * Adapter sterujący (driving) — REST API zamówień Sprzedaży i CRM.
 *
 * UC-CRM-04: umówienie odbioru pojazdu (schedule-handover).
 * UC-CRM-05: rejestracja fizycznego wydania pojazdu ("przycisk potwierdzenia wydania").
 * Anulowanie: rezygnacja klienta z zamówienia (z powodem).
 */
@RestController
@RequestMapping("/api/sales/orders")
public class OrderRestApiAdapter {

    private final SalesService salesAppService;

    public OrderRestApiAdapter(SalesService salesAppService) {
        if (salesAppService == null) {
            throw new IllegalArgumentException("salesAppService must not be null.");
        }
        this.salesAppService = salesAppService;
    }

    /** UC-CRM-04, krok 4-5: Handlowiec wprowadza uzgodniony termin odbioru. */
    @PostMapping("/{orderId}/schedule-handover")
    public ResponseEntity<Void> scheduleHandover(@PathVariable String orderId,
                                                 @Valid @RequestBody ScheduleHandoverRequest request) {
        salesAppService.scheduleHandover(new ScheduleHandoverCommand(
                orderId, LocalDate.parse(request.handoverDate())));
        return ResponseEntity.ok().build();
    }

    /** UC-CRM-05, krok 2-4: Handlowiec potwierdza fizyczne wydanie pojazdu. */
    @PostMapping("/{orderId}/handover")
    public ResponseEntity<Void> confirmHandover(@PathVariable String orderId) {
        salesAppService.confirmHandover(new OrderId(orderId));
        return ResponseEntity.ok().build();
    }

    /** Anulowanie zamówienia (rezygnacja klienta) — z powodem dla Rozliczeń. */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable String orderId,
                                            @RequestBody CancelOrderRequest request) {
        salesAppService.cancelOrder(orderId, request.reason());
        return ResponseEntity.ok().build();
    }

    // Brak zamówienia -> 404 Not Found.
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(OrderNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    // Walidacja wejścia (puste/błędne pola) -> 400 Bad Request, zanim żądanie trafi do domeny.
    @ExceptionHandler({MethodArgumentNotValidException.class, DateTimeParseException.class})
    public ResponseEntity<Map<String, String>> handleInvalidInput(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Invalid input parameters"));
    }

    // Błędne parametry biznesowe (np. data z przeszłości) -> 400 Bad Request.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    // Nieprawidłowy stan zamówienia -> 409 Conflict.
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }
}
