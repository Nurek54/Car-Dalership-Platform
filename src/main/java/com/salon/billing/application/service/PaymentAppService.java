package main.java.com.salon.billing.application.service;

import main.java.com.salon.billing.application.port.in.RegisterPaymentCommand;
import main.java.com.salon.billing.application.port.in.RegisterPaymentUseCase;
import main.java.com.salon.billing.application.port.out.EventPublisherPort;
import main.java.com.salon.billing.application.port.out.PaymentGatewayPort;
import main.java.com.salon.billing.application.port.out.PaymentRepository;
import main.java.com.salon.billing.domain.event.AdvanceRegisteredEvent;
import main.java.com.salon.billing.domain.event.DepositRegisteredEvent;
import main.java.com.salon.billing.domain.model.payment.Payment;
import main.java.com.salon.billing.domain.model.payment.PaymentId;
import main.java.com.salon.billing.domain.model.shared.Money;
import main.java.com.salon.billing.domain.model.shared.OrderId;
import main.java.com.salon.billing.domain.service.PaymentClassificationService;

import java.time.Instant;

/**
 * Realizuje UC-ROZ-01. TYLKO orkiestracja (warstwa aplikacji):
 * pobiera/zapisuje agregaty, woła ich metody biznesowe, publikuje zdarzenia.
 * Żadnej logiki biznesowej tutaj — ta jest w domenie (agregat + serwis dziedzinowy).
 *
 * Zależności wstrzykiwane przez konstruktor (bez frameworka).
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
        // Walidacja wstrzykiwanych zależności — nie pozwalamy zbudować serwisu z nullem.
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
    // @Transactional w projekcie ze Springiem (tu otwieramy transakcję).
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

        // 3. Tworzymy agregat i klasyfikujemy wpłatę (krok 3 UC).
        Payment payment = new Payment(PaymentId.generate(), orderId, amount);
        payment.categorizePayment(requiredDeposit);

        // 4-5. Zapis w ewidencji (aktualizacja salda następuje w innym kontekście po zdarzeniu).
        paymentRepository.save(payment);

        // Jeśli wpłata przyszła z bramki (webhook) — potwierdzamy do bramki.
        if (command.gatewayTransactionId() != null && !command.gatewayTransactionId().isBlank()) {
            paymentGateway.acknowledgePayment(command.gatewayTransactionId());
        }

        // 6. Publikacja zdarzenia (czasownik w czasie przeszłym — sekcja 3.4).
        if (payment.isDeposit()) {
            // Zadatek -> ZadatekZaksiegowany (odblokowanie realizacji zamówienia).
            DepositRegisteredEvent event = new DepositRegisteredEvent(
                    payment.getId().value(),
                    payment.getOrderId().value(),
                    Instant.now());
            eventPublisher.publish(event);
        } else {
            // A2: zaliczka -> NIE emitujemy ZadatekZaksiegowany. Zamiast tego powiadamiamy Handlowca.
            AdvanceRegisteredEvent event = new AdvanceRegisteredEvent(
                    payment.getId().value(),
                    payment.getOrderId().value(),
                    Instant.now());
            eventPublisher.publish(event);
        }

        // Uwaga: A1 (nierozpoznany przelew) obsługiwany jest wcześniej,
        // na etapie identyfikacji zamówienia — zanim powstanie ta komenda.
    }
}
