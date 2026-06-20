package salon.logistics.infrastructure.out.mock;

import salon.logistics.application.port.out.SpecificationReadModelPort;
import salon.common.model.OrderId;
import salon.common.model.SpecificationId;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Adapter wyjściowy (in-memory) portu {@link SpecificationReadModelPort} — lokalna
 * kopia specyfikacji Kontekstu Inwentarza, zasilana zdarzeniami SpecificationCompleted
 * (Katalog) i OrderPlaced (Sprzedaż). W środowisku docelowym zastąpi go adapter
 * bazodanowy (tabela read modelu w schemacie Inwentarza).
 */
public class InMemorySpecificationReadModelAdapter implements SpecificationReadModelPort {

    private final Map<String, List<String>> codesBySpecification = new ConcurrentHashMap<>();
    private final Map<String, String> specificationByOrder = new ConcurrentHashMap<>();

    @Override
    public void saveSpecification(SpecificationId specificationId, List<String> optionCodes) {
        if (specificationId == null) {
            throw new IllegalArgumentException("specificationId must not be null.");
        }
        this.codesBySpecification.put(specificationId.value(),
                optionCodes == null ? List.of() : List.copyOf(optionCodes));
    }

    @Override
    public void linkOrderToSpecification(OrderId orderId, SpecificationId specificationId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        if (specificationId == null) {
            throw new IllegalArgumentException("specificationId must not be null.");
        }
        this.specificationByOrder.put(orderId.value(), specificationId.value());
    }

    @Override
    public Optional<List<String>> findCodesForOrder(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        return Optional.ofNullable(this.specificationByOrder.get(orderId.value()))
                .map(this.codesBySpecification::get);
    }
}
