package salon.sales.application.port.out;

import salon.common.model.Money;
import salon.common.model.SpecificationId;

import java.util.Optional;

/**
 * Port wyjściowy (driven) — lokalna kopia wyceny specyfikacji w Kontekście Sprzedaży
 * (read model, event-carried state transfer).
 *
 * Zastępuje synchroniczne odpytywanie Katalogu (dawne CatalogDatabaseRepository.getSpecificationPrice):
 * wyliczona cena katalogowa przyjeżdża asynchronicznie w zdarzeniu SpecificationCompleted
 * (Katalog). W momencie generowania oferty (UC-CRM-02) Sprzedaż czyta wyłącznie własne dane —
 * żadnej komunikacji synchronicznej między kontekstami.
 */
public interface SpecificationPriceReadModelPort {

    /** Zapis/aktualizacja wyceny specyfikacji (ze zdarzenia SpecificationCompleted). */
    void saveSpecificationPrice(SpecificationId specificationId, Money price);

    /** Wycena specyfikacji (pusty Optional = cena jeszcze nie dotarła zdarzeniem). */
    Optional<Money> findPrice(SpecificationId specificationId);
}
