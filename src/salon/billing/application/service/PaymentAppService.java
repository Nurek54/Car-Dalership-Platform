package salon.billing.application.service;

import salon.billing.application.port.in.RegisterPaymentCommand;
import salon.billing.application.port.in.RegisterPaymentUseCase;
import salon.billing.application.port.out.PaymentGatewayPort;
import salon.billing.application.port.out.PaymentRepository;
import salon.billing.domain.model.payment.Payment;
import salon.billing.domain.model.payment.PaymentId;
import salon.billing.domain.service.PaymentClassificationService;
import salon.shared.application.EventPublisherPort;
import salon.shared.event.DomainEvent;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

/**
 * Realizuje UC-ROZ-01. TYLKO orkiestracja: pobiera/zapisuje agregaty, woła metody biznesowe,
 * publikuje zdarzenia WYGENEROWANE PRZEZ AGREGAT. Decyzja "jakie zdarzenie" siedzi w domenie
 * (Payment.categorizePayment) — serwis nie zagląda już do stanu wpłaty.
 */
public class PaymentAppService implements RegisterPaymentUseCase {

    private final PaymentRepository paymentRepository;
    private final EventPublisherPort eventPublisher;
    private final PaymentGatewayPort paymentGateway;
    private final PaymentClassificationService classificationService;

    public PaymentAppService(PaymentRepository paymentRepository,
                             EventPublisherPort eventPublisher,
                             PaymentGatewayPort paymentGateway,
                             PaymentClassificationService classificationService) {
        if (paymentRepository == null) {
            throw new IllegalArgumentException("paymentRepository must not be null.");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        if (paymentGateway == null) {
            throw new IllegalArgumentException("paymentGateway must not be null.");
        }
        if (classificationService == null) {
            throw new IllegalArgumentException("classificationService must not be null.");
        }
        this.paymentRepository = paymentRepository;
        this.eventPublisher = eventPublisher;
        this.paymentGateway = paymentGateway;
        this.classificationService = classificationService;
    }

    @Override
    // @Transactional w projekcie ze Springiem.
    public void registerPayment(RegisterPaymentCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null.");
        }

        // 1. Dane wejściowe -> obiekty domenowe.
        OrderId orderId = new OrderId(command.orderId());
        Money amount = new Money(command.amount(), command.currency());
        Money orderValue = new Money(command.orderValue(), command.currency());

        // 2. Reguła biznesowa: wymagany zadatek (serwis dziedzinowy).
        Money requiredDeposit = classificationService.calculateRequiredDeposit(orderValue);

        // 3. Tworzymy agregat i klasyfikujemy wpłatę (agregat sam rejestruje zdarzenie).
        Payment payment = new Payment(PaymentId.generate(), orderId, amount);
        payment.categorizePayment(requiredDeposit);

        // 4-5. Zapis w ewidencji.
        paymentRepository.save(payment);

        // Jeśli wpłata przyszła z bramki (webhook) — potwierdzamy do bramki.
        if (command.gatewayTransactionId() != null && !command.gatewayTransactionId().isBlank()) {
            paymentGateway.acknowledgePayment(command.gatewayTransactionId());
        }

        // 6. Publikacja zdarzeń wygenerowanych przez agregat — bez zaglądania w jego stan.
        for (DomainEvent event : payment.pullDomainEvents()) {
            eventPublisher.publish(event);
        }
    }
}
