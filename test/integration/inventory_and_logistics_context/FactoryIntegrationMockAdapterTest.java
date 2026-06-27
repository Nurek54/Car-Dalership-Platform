package integration.inventory_and_logistics_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import salon.logistics.application.port.out.ImporterACL;
import salon.logistics.infrastructure.out.mock.FactoryIntegrationMockAdapter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** Integracja adaptera ACL fabryki (zlecenie produkcji zwraca nadany numer VIN). */
@SpringBootTest(classes = FactoryIntegrationMockAdapter.class)
class FactoryIntegrationMockAdapterTest {

    @Autowired private ImporterACL adapter;

    @Test
    void shouldReturnAssignedVinForFactoryOrder() {
        // Zlecenie produkcji do fabryki kończy się nadaniem numeru VIN
        String vin = adapter.placeFactoryOrder("ORD-1", List.of("B2", "C1"));

        assertThat(vin).isNotBlank().startsWith("VIN-");
    }
}
