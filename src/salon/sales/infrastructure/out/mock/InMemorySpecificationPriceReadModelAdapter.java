package salon.sales.infrastructure.out.mock;

import salon.sales.application.port.out.SpecificationPriceReadModelPort;
import salon.common.model.Money;
import salon.common.model.SpecificationId;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter wyjściowy (in-memory) portu {@link SpecificationPriceReadModelPort} — lokalna
 * kopia wyceny specyfikacji Kontekstu Sprzedaży, zasilana zdarzeniami SpecificationCompleted
 * (Katalog). W środowisku docelowym zastąpi go adapter bazodanowy (tabela read modelu
 * w schemacie Sprzedaży).
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
