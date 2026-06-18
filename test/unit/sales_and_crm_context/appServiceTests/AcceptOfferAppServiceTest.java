package unit.sales_and_crm_context.appServiceTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.sales.application.port.out.OfferDatabaseRepository;
import salon.sales.application.domain.exception.DatabaseException;
import salon.sales.application.domain.exception.OfferNotFoundException;
import salon.sales.application.port.out.OrderDatabaseRepository;
import salon.sales.application.service.SalesService;
import salon.sales.application.domain.model.customer.CustomerId;
import salon.sales.application.domain.model.offer.Offer;
import salon.sales.application.domain.model.offer.OfferId;
import salon.sales.application.domain.model.order.Order;
import salon.common.application.EventPublisher;
import salon.common.model.Money;
import salon.common.model.SpecificationId;

import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/** UC-CRM-03: Zatwierdzenie oferty i utworzenie zamówienia */
@ExtendWith(MockitoExtension.class)
class AcceptOfferAppServiceTest {

    @Mock
    private OfferDatabaseRepository offerRepository;
    @Mock private OrderDatabaseRepository orderRepository;
    @Mock private EventPublisher eventPublisher;
    @InjectMocks
    private SalesService salesAppService;

    @Test
    void shouldAcceptOfferAndSaveOrder() {
        // W bazie znajduje się opublikowana oferta
        OfferId offerId = new OfferId("O-100");
        Offer offer = new Offer(offerId, new CustomerId("C-1"), new SpecificationId("S-1"), Money.of(150000, "PLN"));
        offer.publishOffer();
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(offer));

        // Klika przycisk akceptacji i utworzenia zamówienia (wywołanie serwisu)
        salesAppService.acceptOfferAndCreateOrder(offerId);

        // Zapisujemy zmieniony stan oferty (jako ACCEPTED)
        verify(offerRepository).save(offer);
        // Zapisujemy nowo powstałe zamówienie do bazy
        verify(orderRepository).save(any(Order.class));
        // Publikujemy zdarzenia domenowe na zewnątrz
        verify(eventPublisher).publishAll(anyList());
    }

    @Test
    void shouldRollbackAndNotPublishEventsWhenDatabaseFails() {
        // Repozytorium zamówień ulega awarii podczas próby zapisu
        OfferId offerId = new OfferId("O-101");
        Offer offer = new Offer(offerId, new CustomerId("C-1"), new SpecificationId("S-1"));
        offer.publishOffer();
        when(offerRepository.findById(offerId)).thenReturn(Optional.of(offer));

        doThrow(new DatabaseException("Connection lost")).when(orderRepository).save(any(Order.class));

        // Cały Use Case rzuca błąd, przerywając transakcję
        assertThatThrownBy(() -> salesAppService.acceptOfferAndCreateOrder(offerId))
                .isInstanceOf(DatabaseException.class);

        // Nie wysyłamy zdarzeń domenowych
        verify(eventPublisher, never()).publishAll(anyList());
    }

    @Test
    void shouldThrowExceptionWhenOfferNotFound() {
        // Użytkownik przesyła złe ID oferty
        OfferId fakeId = new OfferId("O-999-UNKNOWN");
        when(offerRepository.findById(fakeId)).thenReturn(Optional.empty());

        // Serwis zatrzymuje proces na samym początku
        assertThatThrownBy(() -> salesAppService.acceptOfferAndCreateOrder(fakeId))
                .isInstanceOf(OfferNotFoundException.class)
                .hasMessageContaining("Offer with ID O-999-UNKNOWN not found in the system");

        // Sprawdzamy, czy nic nie zostało nadpisane w żadnej bazie ani wysłane
        verify(offerRepository, never()).save(any());
        verify(orderRepository, never()).save(any());
        verify(eventPublisher, never()).publishAll(any());
    }

    @Test
    void offerRejected() {
        OfferId offerId = new OfferId("O-101");
        Offer offer = new Offer(offerId, new CustomerId("C-1"), new SpecificationId("S-1"));
        offer.publishOffer();
        offer.reject();

        verify(eventPublisher, never()).publishAll(anyList());
    }
}