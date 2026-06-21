package salon.logistics.application.port.out;

import salon.logistics.application.domain.exception.FactoryOrderFailedException;

import java.util.List;

/**
 * OUTBOUND PORT (Figure 37) – "ImporterACL".
 *
 * Anti-corruption layer (ACL) for the integration with the factory/importer system:
 * the adapter translates the local production request into the external API format and returns the assigned
 * VIN. It reports failure as {@link FactoryOrderFailedException} (UC-INW-02 / A1).
 */
public interface ImporterACL {

    /**
     * Places the production order and returns the VIN assigned by the factory.
     *
     * @throws FactoryOrderFailedException when the factory rejects the order / an integration error occurs
     */
    String placeFactoryOrder(String orderId, List<String> optionCodes);
}
