package salon.financing.infrastructure.out.integration;

import salon.common.model.Money;
import salon.common.model.OrderId;
import salon.financing.application.domain.model.financing.BuyerDetails;
import salon.financing.application.port.out.SalesIntegration;
import salon.sales.api.CustomerSnapshotDto;
import salon.sales.api.SalesQueryFacade;

/**
 * OUTBOUND ADAPTER (ACL, Figure 42: SalesQueryService) — implementation of the {@link SalesIntegration} port.
 *
 * Anti-Corruption Layer: it depends only on the public Sales facade
 * ({@link SalesQueryFacade}, the Published Language) and translates its snapshots into the Financing-local model:
 * CustomerSnapshotDto -> {@link BuyerDetails}, and the offer's final price into the amount to be financed
 * ({@link Money} ze Wspolnego Jadra).
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
    public BuyerDetails buyerDetails(String orderId) {
        CustomerSnapshotDto buyer = this.salesQueryFacade.findBuyerForOrder(new OrderId(orderId));
        return new BuyerDetails(buyer.fullName(), buyer.nip());
    }

    @Override
    public Money offerFinalPrice(String orderId) {
        return this.salesQueryFacade.findOfferForOrder(new OrderId(orderId)).finalPrice();
    }
}
