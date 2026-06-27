package integration.catalog_and_configurator_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;
import salon.catalog.application.domain.model.specification.SpecificationId;
import salon.catalog.application.domain.model.specification.SpecificationState;
import salon.catalog.application.domain.model.specification.VehicleSpecification;
import salon.catalog.application.domain.model.specification.VehicleSpecificationFactory;
import salon.catalog.application.port.out.VehicleSpecificationRepository;
import salon.catalog.infrastructure.out.persistence.InMemoryDbAdapter;
import salon.catalog.infrastructure.out.persistence.SpecificationDatabaseRepository;
import salon.catalog.infrastructure.out.persistence.VehicleSpecificationPersistenceMapper;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/** Integracja warstwy persystencji agregatu VehicleSpecification (adapter + mapper + magazyn). */
@SpringBootTest(classes = {
        SpecificationDatabaseRepository.class,
        VehicleSpecificationPersistenceMapper.class,
        InMemoryDbAdapter.class,
        VehicleSpecificationFactory.class})
class SpecificationDatabaseAdapterTest {

    @Autowired private VehicleSpecificationRepository repository;
    @Autowired private VehicleSpecificationFactory factory;

    @Test
    void shouldSaveAndLoadSpecificationWithOptions() {
        // Specyfikacja w toku z dwiema opcjami i przeliczoną ceną łączną
        VehicleSpecification spec = factory.reconstitute(
                SpecificationId.generate(), CatalogId.generate(),
                Money.of(new BigDecimal("15000"), "PLN"),
                SpecificationState.IN_PROGRESS,
                Set.of(OptionCode.of("B2"), OptionCode.of("C1")));

        repository.save(spec);
        Optional<VehicleSpecification> loaded = repository.findById(spec.id());

        // Stan, cena i zestaw opcji są poprawnie odtworzone z magazynu
        assertThat(loaded).isPresent();
        assertThat(loaded.get().state()).isEqualTo(SpecificationState.IN_PROGRESS);
        assertThat(loaded.get().totalPrice()).isEqualTo(Money.of(new BigDecimal("15000"), "PLN"));
        assertThat(loaded.get().optionsPicked())
                .containsExactlyInAnyOrder(OptionCode.of("B2"), OptionCode.of("C1"));
    }
}
