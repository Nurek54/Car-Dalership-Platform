package unit.sales_and_crm_context.appServiceTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import salon.sales.application.port.out.OfferDatabaseRepository;
import salon.sales.application.port.out.SpecificationPriceReadModelPort;
import salon.sales.application.service.SalesService;
import salon.sales.application.domain.exception.SpecificationNotFoundException;
import salon.sales.application.domain.model.offer.Offer;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.common.model.Money;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** UC-CRM-02: Wygenerowanie oferty proforma (cena z read modelu zasilonego zdarzeniem). */
@ExtendWith(MockitoExtension.class)
class GenerateOfferAppServiceTest {

    @Mock
    private OfferDatabaseRepository offerRepository;
    @Mock
    private SpecificationPriceReadModelPort specificationPriceReadModel;
    @InjectMocks
    private SalesService salesAppService;

    @Test
    void shouldGenerateAndSaveOfferSuccessfully() {
        // Lokalny read model zna już wycenę specyfikacji (przyszła zdarzeniem SpecificationCompleted)
        when(specificationPriceReadModel.findPrice(any()))
                .thenReturn(Optional.of(Money.of(150000, "PLN")));

        // Wywołujemy przypadek użycia generowania oferty
        salesAppService.generateOffer("CUST-1", "SPEC-1");

        // Nowa oferta zostaje poprawnie skonstruowana i przekazana do zapisu w repozytorium
        verify(offerRepository).save(any(Offer.class));
    }

    @Test
    void shouldFailToGenerateOfferWhenSpecificationPriceNotYetAvailable() {
        // Zdarzenie SpecificationCompleted jeszcze nie dotarło — brak wyceny w read modelu
        when(specificationPriceReadModel.findPrice(any())).thenReturn(Optional.empty());

        // Usługa sprzedażowa odrzuca proces — brak wyceny = brak oferty
        assertThatThrownBy(() -> salesAppService.generateOffer("CUST-1", "SPEC-999"))
                .isInstanceOf(SpecificationNotFoundException.class);

        // Żadna wadliwa oferta nie zostaje zapisana w naszej bazie CRM
        verify(offerRepository, never()).save(any());
    }
}
