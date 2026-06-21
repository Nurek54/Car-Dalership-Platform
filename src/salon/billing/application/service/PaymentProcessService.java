package salon.billing.application.service;

import salon.billing.application.command.ProcessPaymentCommand;
import salon.billing.application.domain.exception.SettlementNotFoundException;
import salon.billing.application.domain.model.settlement.Settlement;
import salon.billing.application.domain.model.settlement.SettlementFactory;
import salon.billing.application.domain.model.settlement.SettlementStatus;
import salon.billing.application.port.in.ProcessPayment;
import salon.billing.application.port.out.DocumentDatabaseRepository;
import salon.billing.application.port.out.NotificationGeneration;
import salon.billing.application.port.out.SettlementDatabaseRepository;
import salon.common.application.EventPublisher;
import salon.common.model.Money;
import salon.common.model.OrderId;

/**
 * APPLICATION SERVICE (Fig. 48 — PaymentProcessService) — orchestration of the order balance (UC-FIR-03).
 *
 * Facade type: it begins and closes the use case's logical transaction, coordinates the aggregate
 * the Settlement and the outbound ports and publishes events on success. It contains no rules itself
 * business ones — those are in the {@link Settlement} aggregate. It implements the {@link ProcessPayment} port.
 */
public class PaymentProcessService implements ProcessPayment {

    private final SettlementDatabaseRepository settlementRepository;
    private final SettlementFactory settlementFactory;
    private final DocumentDatabaseRepository documentRepository;
    private final NotificationGeneration notification;
    private final EventPublisher eventPublisher;

    public PaymentProcessService(SettlementDatabaseRepository settlementRepository,
                                 SettlementFactory settlementFactory,
                                 DocumentDatabaseRepository documentRepository,
                                 NotificationGeneration notification,
                                 EventPublisher eventPublisher) {
        if (settlementRepository == null) {
            throw new IllegalArgumentException("settlementRepository must not be null.");
        }
        if (settlementFactory == null) {
            throw new IllegalArgumentException("settlementFactory must not be null.");
        }
        if (documentRepository == null) {
            throw new IllegalArgumentException("documentRepository must not be null.");
        }
        if (notification == null) {
            throw new IllegalArgumentException("notification must not be null.");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        this.settlementRepository = settlementRepository;
        this.settlementFactory = settlementFactory;
        this.documentRepository = documentRepository;
        this.notification = notification;
        this.eventPublisher = eventPublisher;
    }

    /** Initializes the balance (OPEN status) for a new order — the factory creates the aggregate. */
    @Override
    public void initializeSettlement(OrderId orderId, Money totalAmount) {
        Settlement settlement = this.settlementFactory.createNew(orderId, totalAmount);
        this.settlementRepository.save(settlement);
    }

    /**
     * UC-FIR-03: posting the matched transfer. The aggregate records events (collect &amp; pull),
     * the service pulls them after saving and publishes them (PaymentRegistered, possibly AdvancePaymentRegistered/SettlementCompleted).
     */
    @Override
    public void processPayment(ProcessPaymentCommand command) {
        OrderId orderId = new OrderId(command.orderId());
        Settlement settlement = this.settlementRepository.findByOrderId(orderId)
                .orElseThrow(() -> new SettlementNotFoundException(
                        "No open balance for order " + command.orderId()));

        settlement.registerPayment(command.transactionId(),
                Money.of(command.amount(), command.currency()));
        this.settlementRepository.save(settlement);

        this.eventPublisher.publishAll(settlement.pullDomainEvents());
    }

    /** Periodic task: reminder for every unsettled balance (status != SETTLED). */
    @Override
    public void sendPaymentReminders() {
        for (Settlement settlement : this.settlementRepository.findAll()) {
            if (settlement.status() != SettlementStatus.SETTLED) {
                this.notification.notifyPaymentReminder(
                        settlement.orderId(), settlement.outstandingBalance());
            }
        }
    }
}
