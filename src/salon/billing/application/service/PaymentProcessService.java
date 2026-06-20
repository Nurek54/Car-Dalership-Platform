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
 * USLUGA APLIKACJI (Rys. 48 — PaymentProcessService) — orkiestracja salda zamowienia (UC-FIR-03).
 *
 * Typu fasada: rozpoczyna i domyka logiczna transakcje przypadku uzycia, koordynuje agregat
 * Rozliczenia oraz porty wyjsciowe i publikuje zdarzenia po sukcesie. Sama nie zawiera regul
 * biznesowych — te sa w agregacie {@link Settlement}. Realizuje port {@link ProcessPayment}.
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

    /** Inicjalizacja salda (status OPEN) dla nowego zamowienia — fabryka tworzy agregat. */
    @Override
    public void initializeSettlement(OrderId orderId, Money totalAmount) {
        Settlement settlement = this.settlementFactory.createNew(orderId, totalAmount);
        this.settlementRepository.save(settlement);
    }

    /**
     * UC-FIR-03: zaksiegowanie sparowanego przelewu. Agregat rejestruje zdarzenia (collect &amp; pull),
     * usluga sciaga je po zapisie i publikuje (PaymentRegistered, ew. AdvancePaymentRegistered/SettlementCompleted).
     */
    @Override
    public void processPayment(ProcessPaymentCommand command) {
        OrderId orderId = new OrderId(command.orderId());
        Settlement settlement = this.settlementRepository.findByOrderId(orderId)
                .orElseThrow(() -> new SettlementNotFoundException(
                        "Brak otwartego salda dla zamowienia " + command.orderId()));

        settlement.registerPayment(command.transactionId(),
                Money.of(command.amount(), command.currency()));
        this.settlementRepository.save(settlement);

        this.eventPublisher.publishAll(settlement.pullDomainEvents());
    }

    /** Zadanie cykliczne: przypomnienie o kazdym nierozliczonym saldzie (status != SETTLED). */
    @Override
    public void sendPaymentReminders() {
        for (Settlement settlement : this.settlementRepository.findAll()) {
            if (settlement.getStatus() != SettlementStatus.SETTLED) {
                this.notification.notifyPaymentReminder(
                        settlement.getOrderId(), settlement.getOutstandingBalance());
            }
        }
    }
}
