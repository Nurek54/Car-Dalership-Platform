package salon.catalog.application.domain.policy;

public interface Specification<T> {

    boolean isSatisfiedBy(T candidate);
}
