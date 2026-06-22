package salon.sales.application.port.out;

/**
 * OUTBOUND PORT — "AfterSalesIntegration". On vehicle handover (UC-CRM-05) opens the after-sales
 * service/warranty window for the order.
 */
public interface AfterSalesIntegrationPort {

    void openServiceWindow(String orderId);
}
