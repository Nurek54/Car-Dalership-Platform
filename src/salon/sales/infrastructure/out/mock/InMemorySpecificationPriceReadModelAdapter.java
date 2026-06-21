package salon.sales.infrastructure.out.mock;

import salon.sales.application.port.out.SpecificationPriceReadModelPort;
import salon.common.model.Money;
import salon.common.model.SpecificationId;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Outbound adapter (in-memory) of the {@link SpecificationPriceReadModelPort} port — the local
 * copy of the Sales Context specification pricing, fed by SpecificationCompleted events
 * (Catalog). In the target environment it will be replaced by a database adapter (a read-model table
 * in the Sales schema).
 */
public class InMemorySpecificationPriceReadModelAdapter implements SpecificationPriceReadModelPort {

    private final Map<String, Money> priceBySpecification = new ConcurrentHashMap<>();

    @Override
    public void saveSpecificationPrice(SpecificationId specificationId, Money price) {
        if (specificationId == null) {
            throw new IllegalArgumentException("specificationId must not be null.");
        }
        if (price == null) {
            throw new IllegalArgumentException("price must not be null.");
        }
        this.priceBySpecification.put(specificationId.value(), price);
    }

    @Override
    public Optional<Money> findPrice(SpecificationId specificationId) {
        if (specificationId == null) {
            throw new IllegalArgumentException("specificationId must not be null.");
        }
        return Optional.ofNullable(this.priceBySpecification.get(specificationId.value()));
    }
}
