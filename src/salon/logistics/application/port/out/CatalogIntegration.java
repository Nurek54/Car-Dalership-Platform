package salon.logistics.application.port.out;

import java.util.List;
import java.util.Optional;

/**
 * PORT WYJŚCIOWY (Rysunek 37) – „CatalogIntegration”.
 *
 * Dostęp do danych specyfikacji z Kontekstu Katalogu. W wariancie zdarzeniowym (zgodnie
 * z założeniami kanwy: event-carried state transfer) adapter utrzymuje lokalną kopię:
 * specyfikacja -> kody wyposażenia oraz zamówienie -> specyfikacja, zasilaną zdarzeniami,
 * dzięki czemu UC-INW-01/02 nie wymagają synchronicznego odpytywania Katalogu.
 */
public interface CatalogIntegration {

    void saveSpecification(String specificationId, List<String> optionCodes);

    void linkOrderToSpecification(String orderId, String specificationId);

    Optional<String> findSpecificationByOrder(String orderId);

    List<String> findOptionCodes(String specificationId);
}
