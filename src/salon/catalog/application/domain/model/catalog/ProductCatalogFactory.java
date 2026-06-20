package salon.catalog.application.domain.model.catalog;

import salon.catalog.application.domain.exception.CatalogValidationException;
import salon.catalog.application.domain.model.shared.OptionCode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Fabryka agregatu ProductCatalog.
 *
 * Tworzenie poprawnego egzemplarza nie jest odpowiedzialnością agregatu ani klienta.
 * Fabryka hermetyzuje budowę, zapewnia niezmienniki i jest atomowa: albo zwróci
 * poprawny katalog, albo rzuci wyjątek – nigdy nie zwraca obiektu niepoprawnego.
 *
 * Położenie: warstwa dziedziny (fabryka wyraża pojęcie dziedziny – budowę cennika).
 */
public class ProductCatalogFactory {

    /**
     * Tworzy nowy, AKTYWNY katalog w wersji 1 z nowym, globalnym identyfikatorem.
     * Sprawdza niezmienniki: niepusty zbiór opcji, unikatowe kody, reguły wskazujące
     * wyłącznie na opcje istniejące w katalogu.
     */
    public ProductCatalog createNew(ModelYear modelYear,
                                    List<CatalogOption> options,
                                    List<CatalogRule> rules) {
        validate(options, rules);
        return new ProductCatalog(
                CatalogId.generate(),
                modelYear,
                1,
                CatalogState.ACTIVE,
                options,
                rules);
    }

    /**
     * Tworzy AKTYWNY katalog jako kolejną wersję (UC-KON-02 – nowy cennik zastępuje bieżący).
     */
    public ProductCatalog createNextVersion(ModelYear modelYear,
                                            int newVersion,
                                            List<CatalogOption> options,
                                            List<CatalogRule> rules) {
        validate(options, rules);
        return new ProductCatalog(
                CatalogId.generate(),
                modelYear,
                newVersion,
                CatalogState.ACTIVE,
                options,
                rules);
    }

    /**
     * Odtworzenie agregatu z trwałego magazynu (używane przez adapter repozytorium).
     * Nie waliduje reguł biznesowych – dane pochodzą z zaufanego źródła.
     */
    public ProductCatalog reconstitute(CatalogId id,
                                       ModelYear modelYear,
                                       int version,
                                       CatalogState state,
                                       List<CatalogOption> options,
                                       List<CatalogRule> rules) {
        return new ProductCatalog(id, modelYear, version, state, options, rules);
    }

    private void validate(List<CatalogOption> options, List<CatalogRule> rules) {
        if (options == null || options.isEmpty()) {
            throw new CatalogValidationException("Katalog musi zawierać co najmniej jedną opcję");
        }
        Set<OptionCode> codes = new HashSet<>();
        for (CatalogOption option : options) {
            if (!codes.add(option.code())) {
                throw new CatalogValidationException("Zduplikowany kod opcji: " + option.code());
            }
        }
        if (rules != null) {
            for (CatalogRule rule : rules) {
                if (!codes.contains(rule.sourceCode())) {
                    throw new CatalogValidationException(
                            "Reguła odwołuje się do nieistniejącej opcji: " + rule.sourceCode());
                }
                if (!codes.contains(rule.targetCode())) {
                    throw new CatalogValidationException(
                            "Reguła odwołuje się do nieistniejącej opcji: " + rule.targetCode());
                }
            }
        }
    }
}
