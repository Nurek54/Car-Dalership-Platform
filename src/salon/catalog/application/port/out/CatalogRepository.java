package salon.catalog.application.port.out;

import salon.catalog.domain.model.catalog.CatalogId;
import salon.catalog.domain.model.catalog.ProductCatalog;
import salon.shared.model.Money;

import java.util.List;
import java.util.Optional;

/**
 * Port wyjściowy: repozytorium agregatu ProductCatalog (Matryca Produkcyjna / Cennik).
 *
 * Port pełni też rolę punktu odczytu wyceny dla Kontekstu Sprzedaży (UC-CRM-02):
 * getSpecificationPrice zwraca cenę katalogową zatwierdzonej specyfikacji.
 * Implementacja zdalna (CatalogExternalApiAdapter) realizuje to przez HTTP/ACL.
 */
public interface CatalogRepository {

    void save(ProductCatalog catalog);

    Optional<ProductCatalog> findById(CatalogId id);

    List<ProductCatalog> findAll();

    /** UC-CRM-02: wycena zatwierdzonej specyfikacji (cena katalogowa). */
    default Money getSpecificationPrice(String specificationId) {
        throw new UnsupportedOperationException(
                "Specification pricing is not supported by this CatalogRepository implementation.");
    }
}
