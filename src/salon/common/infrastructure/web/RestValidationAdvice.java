package salon.common.infrastructure.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Globalny mapping błędów walidacji wejścia (Bean Validation) na 400 Bad Request
 * w kształcie { "fieldErrors": { "<pole>": "<komunikat>" } }. Wspólny dla adapterów REST.
 *
 * Błędy DZIEDZINOWE (np. OfferExpiredException) mapują lokalne @ExceptionHandler w kontrolerach,
 * bo ten sam wyjątek bywa różnym kodem HTTP zależnie od zasobu (404 vs 409).
 */
@RestControllerAdvice
public class RestValidationAdvice {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        List<FieldError> errors = ex.getBindingResult().getFieldErrors();
        for (int i = 0; i < errors.size(); i++) {
            FieldError error = errors.get(i);
            fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        Map<String, Object> body = new HashMap<>();
        body.put("fieldErrors", fieldErrors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}
