package salon.sales.application.port.out;

/**
 * OUTBOUND PORT — "NotificationIntegration". Alerts the salesperson (UC-CRM-04) to contact the
 * customer and schedule the handover once the order is ready.
 */
public interface NotificationIntegrationPort {

    void sendAlertToSalesperson(String orderId, String message);
}
