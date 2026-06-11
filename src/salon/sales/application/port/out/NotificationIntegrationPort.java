package salon.sales.application.port.out;

/**
 * Port wyjściowy (driven): powiadomienia wewnętrzne dla zespołu sprzedaży
 * (np. alert dla Handlowca o gotowości pojazdu do wydania — UC-CRM-04, krok 2).
 */
public interface NotificationIntegrationPort {

    void sendAlertToSalesperson(String orderId, String message);
}
