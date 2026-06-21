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
 * Global mapping of input validation errors (Bean Validation) to 400 Bad Request
 * in the shape { "fieldErrors": { "<field>": "<message>" } }. Shared by the REST adapters.
 *
 * DOMAIN errors (e.g. OfferExpiredException) are mapped by local @ExceptionHandler in the controllers,
 * because the same exception can be a different HTTP code depending on the resource (404 vs 409).
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
