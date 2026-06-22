package salon.sales.infrastructure.out.persistence;

import org.springframework.stereotype.Component;
import salon.common.model.Money;
import salon.common.model.SpecificationId;
import salon.sales.application.port.out.SpecificationPriceReadModelPort;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SpecificationPriceReadModelAdapter implements SpecificationPriceReadModelPort {

    private final Map<String, Money> prices = new ConcurrentHashMap<>();

    @Override
    public Optional<Money> findPrice(SpecificationId specificationId) {
        return Optional.ofNullable(this.prices.get(specificationId.value()));
    }

    @Override
    public void savePrice(SpecificationId specificationId, Money price) {
        this.prices.put(specificationId.value(), price);
    }
}
