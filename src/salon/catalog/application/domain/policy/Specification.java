package salon.catalog.application.domain.policy;

/**
 * Specification pattern (PDF, chapter 4): a domain-layer value object that is
 * a predicate – a separate business rule returning a boolean value.
 *
 * Implements a rule that is the responsibility of more than one type
 * (here: dependencies between the selected options and the catalog rules).
 *
 * @param <T> the type of object checked by the specification
 */
public interface Specification<T> {

    boolean isSatisfiedBy(T candidate);
}
