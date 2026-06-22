package salon.logistics.application.port.out;

import java.util.List;
import java.util.Optional;

public interface CatalogIntegration {

    void saveSpecification(String specificationId, List<String> optionCodes);

    void linkOrderToSpecification(String orderId, String specificationId);

    Optional<String> findSpecificationByOrder(String orderId);

    List<String> findOptionCodes(String specificationId);
}
