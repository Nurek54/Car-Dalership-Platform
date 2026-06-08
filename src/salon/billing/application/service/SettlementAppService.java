package salon.billing.application.service;

import salon.billing.application.port.in.ProcessPaymentCommand;
import salon.billing.application.port.in.ProcessPaymentUseCase;
import salon.billing.application.port.out.PaymentGatewayPort;
import salon.billing.application.port.out.SettlementRepository;
import salon.billing.domain.model.settlement.Settlement;
import salon.billing.domain.model.settlement.SettlementFactory;
import salon.billing.domain.model.settlement.SettlementStatus;
import salon.shared.application.EventPublisherPort;
import salon.shared.event.DomainEvent;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.util.List;

/**
 * Realizuje UC-FIR-03 (warstwa aplikacji — orkiestracja).
 *
 * Cienka usługa: pobiera/zapisuje agregat Settlement, woła na nim mutacje stanu i publikuje
 * zdarzenia WYGENEROWANE PRZEZ AGREGAT. Cała matematyka salda i decyzja o statusie/zdarzeniu
 * siedzą w agregacie — serwis nie zagląda do jego wewnętrznego stanu.
 */
public class SettlementAppService implements ProcessPaymentUseCase {

    private final SettlementRepository settlementRepository;
    private final SettlementFactory settlementFactory;
    private final EventPublisherPort eventPublisher;
    private final PaymentGatewayPort paymentGateway;

    public SettlementAppService(SettlementRepository settlementRepository,
                                SettlementFactory settlementFactory,
                                EventPublisherPort eventPublisher,
                                PaymentGatewayPort paymentGateway) {
        if (settlementRepository == null) {
            throw new IllegalArgumentException("settlementRepository must not be null.");
        }
        if (settlementFactory == null) {
            throw new IllegalArgumentException("settlementFactory must not be null.");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        if (paymentGateway == null) {
            throw new IllegalArgumentException("paymentGateway must not be null.");
        }
        this.settlementRepository = settlementRepository;
        this.settlementFactory = settlementFactory;
        this.eventPublisher = eventPublisher;
        this.paymentGateway = paymentGateway;
    }

    /**
     * UC-FIR-03, inicjalizacja: złożenie nowego zamówienia. Serwis deleguje budowę agregatu
     * salda do fabryki i zapisuje go w repozytorium.
     */
    public void initializeSettlement(OrderId orderId, Money contractValue) {
        Settlement settlement = this.settlementFactory.createForOrder(orderId, contractValue);
        this.settlementRepository.save(settlement);
        publishEvents(settlement);
    }

    @Override
    // @Transactional w projekcie ze Springiem — wpłata + przeliczenie + zdarzenie w jednej transakcji.
    public void processPayment(ProcessPaymentCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null.");
        }

        OrderId orderId = new OrderId(command.orderId());
        Settlement settlement = this.settlementRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalStateException(
                        "No settlement for order " + command.orderId()));

        Money amount = new Money(command.amount(), command.currency());
        settlement.registerPayment(command.transactionId(), amount);

        this.settlementRepository.save(settlement);

        // Wpłata z bramki (webhook) — potwierdzamy ją do bramki.
        if (command.gatewayTransactionId() != null && !command.gatewayTransactionId().isBlank()) {
            this.paymentGateway.acknowledgePayment(command.gatewayTransactionId());
        }

        publishEvents(settlement);
    }

    /**
     * Cron (PaymentReminderCronJobAdapter): przypomnienia o niepełnych wpłatach.
     * Skanujemy rozliczenia w stanie PARTIAL_PAYMENT — należność nie została jeszcze pokryta.
     */
    public void processPaymentReminders() {
        List<Settlement> all = this.settlementRepository.findAll();
        for (int i = 0; i < all.size(); i++) {
            Settlement settlement = all.get(i);
            if (settlement.getStatus() == SettlementStatus.PARTIAL_PAYMENT) {
                System.out.println("[SettlementAppService] Reminder: order "
                        + settlement.getOrderId().value()
                        + " has an outstanding balance of "
                        + settlement.getOutstandingBalance().amount() + " "
                        + settlement.getOutstandingBalance().currency() + ".");
            }
        }
    }

    private void publishEvents(Settlement settlement) {
        for (DomainEvent event : settlement.pullDomainEvents()) {
            this.eventPublisher.publish(event);
        }
    }
}
