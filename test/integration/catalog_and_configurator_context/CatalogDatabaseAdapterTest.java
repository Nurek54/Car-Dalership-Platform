package integration.catalog_and_configurator_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import salon.catalog.application.domain.model.catalog.CatalogOption;
import salon.catalog.application.domain.model.catalog.CatalogState;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.catalog.ProductCatalog;
import salon.catalog.application.domain.model.catalog.ProductCatalogFactory;
import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;
import salon.catalog.application.port.out.ProductCatalogRepository;
import salon.catalog.infrastructure.out.persistence.CatalogDatabaseRepository;
import salon.catalog.infrastructure.out.persistence.InMemoryDbAdapter;
import salon.catalog.infrastructure.out.persistence.ProductCatalogPersistenceMapper;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** Integracja warstwy persystencji agregatu ProductCatalog (adapter + mapper + magazyn). */
@SpringBootTest(classes = {
        CatalogDatabaseRepository.class,
        ProductCatalogPersistenceMapper.class,
        InMemoryDbAdapter.class,
        ProductCatalogFactory.class})
class CatalogDatabaseAdapterTest {

    @Autowired private ProductCatalogRepository repository;
    @Autowired private ProductCatalogFactory factory;

    private ProductCatalog catalog(int year) {
        return factory.createNew(ModelYear.of(year),
                List.of(new CatalogOption(OptionCode.of("B2"), Money.of(new BigDecimal("10000"), "PLN"))),
                List.of());
    }

    @Test
    void shouldSaveAndLoadCatalog() {
        ProductCatalog catalog = catalog(2025);

        // Zapis i odczyt agregatu przez adapter bazodanowy
        repository.save(catalog);
        Optional<ProductCatalog> loaded = repository.findById(catalog.id());

        // Agregat jest poprawnie odtworzony z magazynu (mapowanie tam i z powrotem)
        assertThat(loaded).isPresent();
        assertThat(loaded.get().version()).isEqualTo(1);
        assertThat(loaded.get().state()).isEqualTo(CatalogState.ACTIVE);
        assertThat(loaded.get().priceOf(OptionCode.of("B2")))
                .isEqualTo(Money.of(new BigDecimal("10000"), "PLN"));
    }

    @Test
    void shouldFindActiveByModelYear() {
        ProductCatalog catalog = catalog(2026);
        repository.save(catalog);

        // Aktywny cennik dla rocznika jest źródłem cen w UC-KON-01
        assertThat(repository.findActiveByModelYear(ModelYear.of(2026))).isPresent();
    }

    @Test
    void shouldNotReturnArchivedCatalogAsActive() {
        ProductCatalog catalog = catalog(2027);
        repository.save(catalog);

        // Po archiwizacji cennik nie jest już zwracany jako aktywny
        catalog.archive();
        repository.save(catalog);

        assertThat(repository.findActiveByModelYear(ModelYear.of(2027))).isEmpty();
    }
}
