package salon.logistics.application.port.out;

import salon.shared.model.OrderId;
import salon.shared.model.SpecificationId;

import java.util.List;
import java.util.Optional;

/**
 * Port wyjściowy (driven) — lokalna kopia specyfikacji pojazdów w Kontekście
 * Inwentarza i Logistyki (read model, event-carried state transfer).
 *
 * Zastępuje synchroniczne odpytywanie Katalogu/Sprzedaży (dawny
 * SpecificationIntegrationPort): kody wyposażenia przyjeżdżają asynchronicznie
 * w zdarzeniu SpecificationCompleted (Katalog), a powiązanie zamówienia ze
 * specyfikacją — w zdarzeniu OrderPlaced (Sprzedaż). W momencie rezerwacji
 * (UC-INW-01) lub zlecenia produkcji (UC-INW-02) Inwentarz czyta wyłącznie
 * własne dane — żadnej komunikacji synchronicznej między kontekstami.
 */
public interface SpecificationReadModelPort {

    /** Zapis/aktualizacja kodów wyposażenia specyfikacji (ze zdarzenia SpecificationCompleted). */
    void saveSpecification(SpecificationId specificationId, List<String> optionCodes);

    /** Powiązanie zamówienia ze specyfikacją (ze zdarzenia OrderPlaced). */
    void linkOrderToSpecification(OrderId orderId, SpecificationId specificationId);

    /** Kody wyposażenia specyfikacji powiązanej z zamówieniem (pusty Optional = dane jeszcze nie dotarły). */
    Optional<List<String>> findCodesForOrder(OrderId orderId);
}
