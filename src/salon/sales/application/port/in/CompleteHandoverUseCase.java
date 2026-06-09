package salon.sales.application.port.in;

/**
 * Port wejściowy dla UC-CRM-05: rejestracja fizycznego wydania pojazdu (Handlowiec
 * potwierdza wydanie w CRM po podpisaniu protokołu).
 */
public interface CompleteHandoverUseCase {
    void completeHandover(String orderId);
}
