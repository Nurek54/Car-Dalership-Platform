package salon.sales.application.port.out;

public interface NotificationIntegrationPort {

    void sendAlertToSalesperson(String orderId, String message);
}
