package salon.sales.infrastructure.in.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import salon.sales.application.domain.exception.OfferNotFoundException;
import salon.sales.application.domain.exception.OrderNotFoundException;

import java.util.Map;

@RestControllerAdvice(assignableTypes = {
        OfferRestApiAdapter.class, OrderRestApiAdapter.class,
        ConfiguratorRestApiAdapter.class, InventoryWebhookRestApiAdapter.class})
public class SalesRestExceptionAdvice {

    @ExceptionHandler(OfferNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleOfferNotFound(OfferNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(OrderNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleOrderNotFound(OrderNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler({MethodArgumentNotValidException.class, HttpMessageNotReadableException.class,
            IllegalArgumentException.class})
    public ResponseEntity<Map<String, String>> handleInvalidInput(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Invalid input parameters"));
    }
}
