package salon.logistics.infrastructure.out.mock;

import salon.logistics.application.port.out.CatalogIntegration;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ADAPTER WYJŚCIOWY (Rysunek 37: CatalogExternalAPI) – implementacja portu
 * {@link CatalogIntegration} w pamięci. Lokalna kopia danych Katalogu zasilana zdarzeniami:
 * specyfikacja -> kody wyposażenia oraz zamówienie -> specyfikacja.
 */
public class InMemorySpecificationReadModelAdapter implements CatalogIntegration {

    private final Map<String, List<String>> optionsBySpecification = new ConcurrentHashMap<>();
    private final Map<String, String> specificationByOrder = new ConcurrentHashMap<>();

    @Override
    public void saveSpecification(String specificationId, List<String> optionCodes) {
        this.optionsBySpecification.put(specificationId, List.copyOf(optionCodes));
    }

    @Override
    public void linkOrderToSpecification(String orderId, String specificationId) {
        this.specificationByOrder.put(orderId, specificationId);
    }

    @Override
    public Optional<String> findSpecificationByOrder(String orderId) {
        return Optional.ofNullable(this.specificationByOrder.get(orderId));
    }

    @Override
    public List<String> findOptionCodes(String specificationId) {
        return this.optionsBySpecification.getOrDefault(specificationId, List.of());
    }
}
