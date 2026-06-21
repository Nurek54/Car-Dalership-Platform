package salon.sales.application.port.out;

/**
 * Outbound port (driven): internal notifications for the sales team
 * (e.g. an alert for the Salesperson about the vehicle's readiness for handover — UC-CRM-04, step 2).
 */
public interface NotificationIntegrationPort {

    void sendAlertToSalesperson(String orderId, String message);
}
