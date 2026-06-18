package salon.sales.application.port.in;

/**
 * Port wejściowy wyzwalany zdarzeniem z Rozliczeń (PaymentRegisteredEvent / WplataZaksiegowana).
 * To realizacja kroku 5 UC-SPR-02: zaksięgowanie zadatku odblokowuje realizację zamówienia.
 */
public interface ActivateOrderOnDeposit {
    void activateOnDeposit(String orderId);
}
