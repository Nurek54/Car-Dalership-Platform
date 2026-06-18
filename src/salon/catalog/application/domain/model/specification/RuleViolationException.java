package salon.catalog.application.domain.model.specification;

/**
 * Wyjątek dziedzinowy: próba dodania opcji łamiącej regułę cennika (np. wzajemne wykluczenie).
 * Rzucany Fail-fast — od razu przy dodawaniu opcji, zanim powstanie niespójna konfiguracja.
 */
public class RuleViolationException extends RuntimeException {

    public RuleViolationException(String message) {
        super(message);
    }
}
