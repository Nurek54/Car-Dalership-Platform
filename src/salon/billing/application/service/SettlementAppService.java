package salon.billing.application.service;

import salon.billing.application.command.ProcessPaymentCommand;
import salon.billing.application.port.in.ProcessPaymentUseCase;
import salon.billing.application.port.out.DocumentRepository;
import salon.billing.application.port.out.NotificationPort;
import salon.billing.application.port.out.SettlementRepository;
import salon.billing.domain.event.PaymentDeadlineExpiredEvent;
import salon.billing.domain.model.document.AccountingDocument;
import salon.billing.domain.model.document.DocumentStatus;
import salon.billing.domain.model.settlement.Settlement;
import salon.billing.domain.model.settlement.SettlementFactory;
import salon.billing.domain.model.settlement.SettlementStatus;
import salon.shared.application.EventPublisherPort;
import salon.shared.event.DomainEvent;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Realizuje UC-FIR-03 oraz WF-FIR-03 (warstwa aplikacji — orkiestracja).
 *
 * Cienka usługa: pobiera/zapisuje agregat Settlement, woła na nim mutacje stanu i publikuje
 * zdarzenia WYGENEROWANE PRZEZ AGREGAT (PaymentRegistered, AdvancePaymentRegistered,
 * SettlementCompleted). Cała matematyka salda i decyzja o statusie/zdarzeniu siedzą
 * w agregacie — serwis nie zagląda do jego wewnętrznego stanu.
 *
 * WF-FIR-03 (monitorowanie i egzekwowanie terminów płatności): cron wyzwala
 * processPaymentReminders() — przypomnienia do klienta idą przez NotificationPort
 * (e-mail z danymi do przelewu i saldem), a przekroczone terminy (dueDate dokumentu)
 * kończą się emisją PaymentDeadlineExpired, na którą reaguje Inwentarz (UC-INW-04).
 */
public class SettlementAppService implements ProcessPaymentUseCase {

    private final SettlementRepository settlementRepository;
    private final SettlementFactory settlementFactory;
    private final DocumentRepository documentRepository;
    private final NotificationPort notification;
    private final EventPublisherPort eventPublisher;

    public SettlementAppService(SettlementRepository settlementRepository,
                                SettlementFactory settlementFactory,
                                DocumentRepository documentRepository,
                                NotificationPort notification,
                                EventPublisherPort eventPublisher) {
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

    /**
     * Inicjalizacja: złożenie nowego zamówienia (OrderPlacedEvent ze Sprzedaży) wyzwala
     * port wejściowy, a usługa deleguje budowę agregatu salda do SettlementFactory
     * "na podstawie zamówienia uzyskanego w zdarzeniu" (PDF rozdz. 3.7.3).
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
        publishEvents(settlement);
    }

    /**
     * Cron (PaymentReminderCronJobAdapter), WF-FIR-03:
     * 1) rozliczenia z niepokrytym saldem -> przypomnienie e-mail przez NotificationPort
     *    (dane do przelewu z wystawionego dokumentu + brakująca kwota),
     * 2) dokumenty po terminie płatności (dueDate < dziś) -> PaymentDeadlineExpired
     *    (Inwentarz zwalnia blokadę pojazdu — UC-INW-04; operacja po jego stronie idempotentna).
     */
    public void processPaymentReminders() {
        LocalDate today = LocalDate.now();
        List<Settlement> all = this.settlementRepository.findAll();
        for (int i = 0; i < all.size(); i++) {
            Settlement settlement = all.get(i);
            if (settlement.getStatus() == SettlementStatus.SETTLED) {
                continue;
            }
            Optional<AccountingDocument> document =
                    findLatestIssuedDocument(settlement.getOrderId());
            if (document.isEmpty()) {
                continue; // nie wystawiono jeszcze żadnego wezwania do zapłaty
            }
            AccountingDocument issued = document.get();
            if (issued.getDueDate() != null && issued.getDueDate().isBefore(today)) {
                this.eventPublisher.publish(new PaymentDeadlineExpiredEvent(
                        UUID.randomUUID(), settlement.getOrderId().value(), Instant.now()));
            } else {
                this.notification.notifyPaymentReminder(
                        issued, settlement.getOutstandingBalance());
            }
        }
    }

    private Optional<AccountingDocument> findLatestIssuedDocument(OrderId orderId) {
        List<AccountingDocument> documents = this.documentRepository.findByOrderId(orderId);
        AccountingDocument latest = null;
        for (int i = 0; i < documents.size(); i++) {
            AccountingDocument candidate = documents.get(i);
            if (candidate.getStatus() != DocumentStatus.ISSUED) {
                continue;
            }
            if (latest == null || (candidate.getIssueDate() != null && latest.getIssueDate() != null
                    && candidate.getIssueDate().isAfter(latest.getIssueDate()))) {
                latest = candidate;
            }
        }
        return Optional.ofNullable(latest);
    }

    private void publishEvents(Settlement settlement) {
        for (DomainEvent event : settlement.pullDomainEvents()) {
            this.eventPublisher.publish(event);
        }
    }
}
