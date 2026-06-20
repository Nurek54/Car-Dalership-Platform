package salon.financing.infrastructure.out.integration;

import salon.financing.application.port.out.SalesIntegration;
import salon.financing.application.domain.model.financing.BuyerDetails;
import salon.sales.api.CustomerSnapshotDto;
import salon.sales.api.OfferSnapshotDto;
import salon.sales.api.SalesQueryFacade;
import salon.common.model.Money;
import salon.common.model.OrderId;

/**
 * Adapter wyjściowy (ACL) portu {@link SalesIntegration} Kontekstu Finansowania.
 *
 * Zna wyłącznie publiczne API Sprzedaży ({@link SalesQueryFacade} + DTO Published
 * Language), a nie jej repozytoria i agregaty. Tłumaczy {@link CustomerSnapshotDto}
 * na lokalny obiekt wartości {@link BuyerDetails}; cena końcowa ({@link Money})
 * pochodzi ze Wspólnego Jądra i nie wymaga translacji.
 *
 * W środowisku rozproszonym fasadę zastąpiłby klient REST do modułu CRM — kontrakt
 * portu pozostaje bez zmian.
 */
public class SalesCrmIntegrationAdapter implements SalesIntegration {

    private final SalesQueryFacade salesQueryFacade;

    public SalesCrmIntegrationAdapter(SalesQueryFacade salesQueryFacade) {
        if (salesQueryFacade == null) {
            throw new IllegalArgumentException("salesQueryFacade must not be null.");
        }
        this.salesQueryFacade = salesQueryFacade;
    }

    @Override
    public BuyerDetails getBuyerDetails(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        CustomerSnapshotDto snapshot = this.salesQueryFacade.findBuyerForOrder(orderId);
        // Translacja Published Language CRM na lokalny obiekt wartości Finansowania.
        return new BuyerDetails(snapshot.fullName(), snapshot.nip());
    }

    @Override
    public Money getOfferFinalPrice(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        OfferSnapshotDto snapshot = this.salesQueryFacade.findOfferForOrder(orderId);
        return snapshot.finalPrice();
    }
}
