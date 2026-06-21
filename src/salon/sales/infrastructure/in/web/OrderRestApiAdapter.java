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
 * Driving adapter — REST API for Sales and CRM orders.
 *
 * UC-CRM-04: scheduling the vehicle handover (schedule-handover).
 * UC-CRM-05: registering the physical vehicle handover ("the handover confirmation button").
 * Cancellation: the customer's withdrawal from the order (with a reason).
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

    /** UC-CRM-04, steps 4-5: the Salesperson enters the agreed pickup date. */
    @PostMapping("/{orderId}/schedule-handover")
    public ResponseEntity<Void> scheduleHandover(@PathVariable String orderId,
                                                 @Valid @RequestBody ScheduleHandoverRequest request) {
        salesAppService.scheduleHandover(new ScheduleHandoverCommand(
                orderId, LocalDate.parse(request.handoverDate())));
        return ResponseEntity.ok().build();
    }

    /** UC-CRM-05, steps 2-4: the Salesperson confirms the physical vehicle handover. */
    @PostMapping("/{orderId}/handover")
    public ResponseEntity<Void> confirmHandover(@PathVariable String orderId) {
        salesAppService.confirmHandover(new OrderId(orderId));
        return ResponseEntity.ok().build();
    }

    /** Cancelling the order (customer cancellation) — with a reason for Billing. */
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable String orderId,
                                            @RequestBody CancelOrderRequest request) {
        salesAppService.cancelOrder(orderId, request.reason());
        return ResponseEntity.ok().build();
    }

    // No order -> 404 Not Found.
    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(OrderNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    // Input validation (empty/invalid fields) -> 400 Bad Request, before the request reaches the domain.
    @ExceptionHandler({MethodArgumentNotValidException.class, DateTimeParseException.class})
    public ResponseEntity<Map<String, String>> handleInvalidInput(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "Invalid input parameters"));
    }

    // Invalid business parameters (e.g. a date in the past) -> 400 Bad Request.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", ex.getMessage()));
    }

    // Incorrect order state -> 409 Conflict.
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<Map<String, String>> handleIllegalState(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
    }
}
