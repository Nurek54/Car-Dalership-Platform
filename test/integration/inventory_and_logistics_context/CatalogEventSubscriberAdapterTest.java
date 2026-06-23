package integration.inventory_and_logistics_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import salon.logistics.application.port.out.CatalogIntegration;
import salon.logistics.infrastructure.in.messaging.CatalogEventSubscriberAdapter;

import java.util.List;

import static org.mockito.Mockito.verify;

/** Adapter nasłuchujący zdarzeń Katalogu — zasila lokalny model odczytowy specyfikacji. */
@SpringBootTest(classes = CatalogEventSubscriberAdapter.class)
class CatalogEventSubscriberAdapterTest {

    @Autowired private CatalogEventSubscriberAdapter adapter;
    @MockBean private CatalogIntegration catalogIntegration;

    @Test
    void shouldSaveSpecificationOnSpecificationCompleted() {
        // Zdarzenie SpecificationCompleted zapisuje kody opcji w lokalnym modelu odczytowym
        adapter.handleSpecificationCompleted(
                new CatalogEventSubscriberAdapter.SpecificationCompleted("SPEC-1", List.of("B2", "C1")));

        verify(catalogIntegration).saveSpecification("SPEC-1", List.of("B2", "C1"));
    }
}
