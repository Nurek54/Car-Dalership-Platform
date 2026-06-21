package salon.logistics.application.port.out;

import java.util.List;
import java.util.Optional;

/**
 * OUTBOUND PORT (Figure 37) – "CatalogIntegration".
 *
 * Access to specification data from the Catalog Context. In the event-driven variant (in line
 * with the canvas assumptions: event-carried state transfer) the adapter keeps a local copy:
 * specification -> equipment codes and order -> specification, fed by events,
 * so that UC-INW-01/02 do not require synchronous querying of the Catalog.
 */
public interface CatalogIntegration {

    void saveSpecification(String specificationId, List<String> optionCodes);

    void linkOrderToSpecification(String orderId, String specificationId);

    Optional<String> findSpecificationByOrder(String orderId);

    List<String> findOptionCodes(String specificationId);
}
