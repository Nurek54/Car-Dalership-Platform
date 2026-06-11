package unit.sales_and_crm_context.appServiceTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import salon.catalog.application.port.out.CatalogRepository;
import salon.sales.application.port.out.OfferRepository;
import salon.sales.application.service.SalesAppService;
import salon.sales.domain.exception.ExternalServiceUnavailableException;
import salon.sales.domain.model.offer.Offer;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.shared.model.Money;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** UC-CRM-02: Wygenerowanie oferty proforma */
@ExtendWith(MockitoExtension.class)
class GenerateOfferAppServiceTest {

    @Mock
    private OfferRepository offerRepository;
    @Mock private CatalogRepository catalogPort;
    @InjectMocks
    private SalesAppService salesAppService;

    @Test
    void shouldGenerateAndSaveOfferSuccessfully() {
        // Moduł Katalogu odpowiada poprawnie i zwraca wycenę specyfikacji na 150 000 PLN
        when(catalogPort.getSpecificationPrice("SPEC-1")).thenReturn(Money.of(150000, "PLN"));

        // Wywołujemy przypadek użycia generowania oferty
        salesAppService.generateOffer("CUST-1", "SPEC-1");

        // Nowa oferta zostaje poprawnie skonstruowana i przekazana do zapisu w repozytorium
        verify(offerRepository).save(any(Offer.class));
    }

    @Test
    void shouldFailToGenerateOfferWhenCatalogServiceIsDown() {
        // Zewnętrzny mikroserwis Katalogu ma awarię i rzuca wyjątkiem
        when(catalogPort.getSpecificationPrice("SPEC-999"))
                .thenThrow(new ExternalServiceUnavailableException("Catalog service timeout"));

        // Nasza usługa sprzedażowa wyłapuje to i odrzuca proces
        assertThatThrownBy(() -> salesAppService.generateOffer("CUST-1", "SPEC-999"))
                .isInstanceOf(ExternalServiceUnavailableException.class);

        // Żadna wadliwa oferta nie zostaje zapisana w naszej bazie CRM
        verify(offerRepository, never()).save(any());
    }
}