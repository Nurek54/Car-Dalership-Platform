package integration.financing_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import salon.common.model.Money;
import salon.common.model.OrderId;
import salon.financing.application.domain.model.financing.BuyerDetails;
import salon.financing.infrastructure.out.integration.SalesCrmIntegrationAdapter;
import salon.sales.api.CustomerSnapshotDto;
import salon.sales.api.OfferSnapshotDto;
import salon.sales.api.SalesQueryFacade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/** Integracja warstwy ACL do Sprzedaży/CRM — dociąganie danych kupującego i ceny oferty. */
@SpringBootTest(classes = SalesCrmIntegrationAdapter.class)
class SalesCrmIntegrationAdapterTest {

    @Autowired private SalesCrmIntegrationAdapter adapter;
    @MockBean private SalesQueryFacade salesQueryFacade;

    @Test
    void shouldTranslateBuyerSnapshotIntoLocalModel() {
        // Fasada Sprzedaży zwraca migawkę kupującego (Published Language)
        when(salesQueryFacade.findBuyerForOrder(new OrderId("ORD-1")))
                .thenReturn(new CustomerSnapshotDto("Jan Kowalski", "1234563218"));

        BuyerDetails buyer = adapter.buyerDetails("ORD-1");

        // Migawka jest tłumaczona na lokalny obiekt wartości BuyerDetails
        assertThat(buyer.name()).isEqualTo("Jan Kowalski");
        assertThat(buyer.nip()).isEqualTo("1234563218");
    }

    @Test
    void shouldFetchOfferFinalPrice() {
        // Fasada Sprzedaży zwraca migawkę oferty z ceną końcową
        when(salesQueryFacade.findOfferForOrder(new OrderId("ORD-1")))
                .thenReturn(new OfferSnapshotDto("OFF-1", Money.of(100000, "PLN")));

        Money price = adapter.offerFinalPrice("ORD-1");

        assertThat(price).isEqualTo(Money.of(100000, "PLN"));
    }
}
