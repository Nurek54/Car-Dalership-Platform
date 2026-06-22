package salon.sales.application.port.out;

import salon.common.model.Money;

public interface BillingIntegration {

    
    void openSettlement(String orderId, Money contractValue);

    
    void requestProformaInvoice(String orderId, Money amount);

    
    void closeOrderBalance(String orderId);

    
    void processCancelledOrderBilling(String orderId, String reason);
}
