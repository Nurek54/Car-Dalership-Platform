package salon.billing.infrastructure.integration;

import salon.billing.application.port.out.CrmIntegrationPort;
import salon.billing.domain.model.document.BuyerDetails;
import salon.sales.api.CustomerSnapshotDto;
import salon.sales.api.SalesQueryFacade;
import salon.shared.model.OrderId;

/**
 * Adapter wyjściowy (ACL) portu {@link CrmIntegrationPort} — realizuje synchroniczne
 * zapytanie (Query) do Kontekstu Sprzedaży i CRM (PDF rozdz. 3.7.3 "CrmIntegrationPort").
 *
 * Adapter zna wyłącznie publiczne API Sprzedaży ({@link SalesQueryFacade} +
 * {@link CustomerSnapshotDto} — Published Language), a nie jej repozytoria i agregaty.
 * Jego jedyną odpowiedzialnością jest translacja DTO Sprzedaży na lokalny obiekt
 * wartości {@link BuyerDetails} kontekstu Fakturowania.
 *
 * Zdarzenia logistyczne z placu (VehicleIsNotOnStock, VehicleReservedFromStock) niosą
 * wyłącznie orderId/VIN — bez danych osobowych.
 *
 * W środowisku rozproszonym fasadę zastąpiłby klient REST do modułu CRM — kontrakt
 * portu pozostaje bez zmian.
 */
public class SalesCrmIntegrationAdapter implements CrmIntegrationPort {

    private final SalesQueryFacade salesQueryFacade;

    public SalesCrmIntegrationAdapter(SalesQueryFacade salesQueryFacade) {
        if (salesQueryFacade == null) {
            throw new IllegalArgumentException("salesQueryFacade must not be null.");
        }
        this.salesQueryFacade = salesQueryFacade;
    }

    @Override
    public BuyerDetails getCustomerDetails(OrderId orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("orderId must not be null.");
        }
        CustomerSnapshotDto snapshot = this.salesQueryFacade.findBuyerForOrder(orderId);

        // Translacja Published Language CRM na lokalny obiekt wartości Fakturowania.
        return new BuyerDetails(snapshot.fullName(), snapshot.nip());
    }
}
