package salon.sales.application.port.in;

/**
 * Port wejściowy wyzwalany zdarzeniem z Rozliczeń (DepositRegisteredEvent / ZadatekZaksiegowany).
 * To realizacja kroku 5 UC-SPR-02: zaksięgowanie zadatku odblokowuje realizację zamówienia.
 */
public interface ActivateOrderOnDepositUseCase {
    void activateOnDeposit(String orderId);
}
