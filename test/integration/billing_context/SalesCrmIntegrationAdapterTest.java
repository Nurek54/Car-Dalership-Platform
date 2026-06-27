package integration.billing_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import salon.billing.application.domain.model.document.BuyerDetails;
import salon.billing.infrastructure.out.integration.SalesCrmIntegrationAdapter;
import salon.common.model.OrderId;
import salon.sales.api.CustomerSnapshotDto;
import salon.sales.api.SalesQueryFacade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** Integracja warstwy ACL do Sprzedaży/CRM — dociąganie i translacja danych nabywcy. */
@SpringBootTest(classes = SalesCrmIntegrationAdapter.class)
class SalesCrmIntegrationAdapterTest {

    @Autowired private SalesCrmIntegrationAdapter adapter;
    @MockBean private SalesQueryFacade salesQueryFacade;

    @Test
    void shouldTranslateBuyerSnapshotIntoLocalModel() {
        // Fasada Sprzedaży zwraca migawkę kupującego (Published Language)
        when(salesQueryFacade.findBuyerForOrder(new OrderId("ORD-1")))
                .thenReturn(new CustomerSnapshotDto("Jan Kowalski", "1234563218"));

        BuyerDetails buyer = adapter.buyerDetailsFor("ORD-1");

        // Migawka tłumaczona na hermetyczny obiekt wartości BuyerDetails (ochrona przed korupcją modelem)
        assertThat(buyer.name()).isEqualTo("Jan Kowalski");
        assertThat(buyer.nip()).isEqualTo("1234563218");
    }
}
