package integration.inventory_and_logistics_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import salon.logistics.application.port.out.CatalogIntegration;
import salon.logistics.infrastructure.out.mock.InMemorySpecificationReadModelAdapter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Integracja lokalnego modelu odczytowego specyfikacji (kody opcji + powiązanie zamówienia). */
@SpringBootTest(classes = InMemorySpecificationReadModelAdapter.class)
class InMemorySpecificationReadModelAdapterTest {

    @Autowired private CatalogIntegration adapter;

    @Test
    void shouldStoreAndReturnOptionCodes() {
        // Kody opcji zapisane dla specyfikacji są poprawnie odczytywane
        adapter.saveSpecification("SPEC-1", List.of("B2", "C1"));

        assertThat(adapter.findOptionCodes("SPEC-1")).containsExactly("B2", "C1");
        assertThat(adapter.findOptionCodes("NIEZNANA")).isEmpty();
    }

    @Test
    void shouldLinkOrderToSpecification() {
        // Powiązanie zamówienia ze specyfikacją (zdarzenie OrderPlaced z Sprzedaży)
        adapter.linkOrderToSpecification("ORD-1", "SPEC-1");

        assertThat(adapter.findSpecificationByOrder("ORD-1")).contains("SPEC-1");
        assertThat(adapter.findSpecificationByOrder("ORD-X")).isEmpty();
    }
}
