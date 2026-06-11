package salon.financing.application;

/**
 * UC-FIN-01, scenariusz A2: system banku natychmiast odrzucił wniosek na etapie walidacji
 * (np. błędny NIP, brak wymaganych danych). Tłumaczona przez ACL na czysty wyjątek domeny.
 */
public class BankValidationException extends RuntimeException {

    public BankValidationException(String message) {
        super(message);
    }
}
