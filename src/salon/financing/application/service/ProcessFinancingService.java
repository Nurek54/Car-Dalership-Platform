package salon.financing.application.service;

import salon.common.application.EventPublisher;
import salon.common.model.Money;
import salon.financing.application.domain.event.FinancingApplicationFailedEvent;
import salon.financing.application.domain.event.FinancingApprovedEvent;
import salon.financing.application.domain.event.FinancingRejectedEvent;
import salon.financing.application.domain.exception.FinancingApplicationNotFoundException;
import salon.financing.application.domain.model.financing.BuyerDetails;
import salon.financing.application.domain.model.financing.CustomerId;
import salon.financing.application.domain.model.financing.FinancingApplication;
import salon.financing.application.domain.model.financing.FinancingApplicationFactory;
import salon.financing.application.domain.model.financing.OrderId;
import salon.financing.application.port.in.ProcessFinancing;
import salon.financing.application.port.out.BankIntegrationAcl;
import salon.financing.application.port.out.FinancingApplicationDatabaseRepository;
import salon.financing.application.port.out.SalesIntegration;

/**
 * USŁUGA APLIKACJI (Rysunek 42) – „ProcessFinancingService”.
 *
 * Jedyny punkt orkiestracji Kontekstu Finansowania. Realizuje port wejściowy {@link ProcessFinancing}
 * (UC-FIN-01/02), korzystając z portów wyjściowych: {@link FinancingApplicationDatabaseRepository},
 * {@link SalesIntegration}, {@link BankIntegrationAcl} oraz wspólnego {@link EventPublisher}.
 * Salon nie podejmuje decyzji kredytowej — usługa jedynie wysyła wniosek i odzwierciedla decyzję banku.
 */
public class ProcessFinancingService implements ProcessFinancing {

    private final FinancingApplicationDatabaseRepository applicationRepository;
    private final FinancingApplicationFactory applicationFactory;
    private final SalesIntegration salesIntegration;
    private final BankIntegrationAcl bankIntegration;
    private final EventPublisher eventPublisher;

    public ProcessFinancingService(FinancingApplicationDatabaseRepository applicationRepository,
                                   FinancingApplicationFactory applicationFactory,
                                   SalesIntegration salesIntegration,
                                   BankIntegrationAcl bankIntegration,
                                   EventPublisher eventPublisher) {
        if (applicationRepository == null) {
            throw new IllegalArgumentException("applicationRepository must not be null.");
        }
        if (applicationFactory == null) {
            throw new IllegalArgumentException("applicationFactory must not be null.");
        }
        if (salesIntegration == null) {
            throw new IllegalArgumentException("salesIntegration must not be null.");
        }
        if (bankIntegration == null) {
            throw new IllegalArgumentException("bankIntegration must not be null.");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        this.applicationRepository = applicationRepository;
        this.applicationFactory = applicationFactory;
        this.salesIntegration = salesIntegration;
        this.bankIntegration = bankIntegration;
        this.eventPublisher = eventPublisher;
    }

    // ===== UC-FIN-01: złożenie wniosku o finansowanie =====

    @Override
    public void requestFinancing(String orderId, String customerId) {
        try {
            // Krok 2: agregacja danych z Kontekstu Sprzedaży (dane nabywcy + kwota do sfinansowania).
            BuyerDetails buyerDetails = this.salesIntegration.fetchBuyerDetails(orderId);
            Money amountToFinance = this.salesIntegration.fetchAmountToFinance(orderId);

            // Krok 3: zbudowanie lokalnego wniosku (DRAFT) — translacja na model dziedziny.
            FinancingApplication application = this.applicationFactory.createDraft(
                    new OrderId(orderId), new CustomerId(customerId), buyerDetails, amountToFinance);

            // Krok 4–5: wysłanie do banku (ACL) i oznaczenie „W trakcie weryfikacji bankowej".
            application.submitApplication();                 // DRAFT -> PENDING (reguła w agregacie)
            this.applicationRepository.save(application);
            this.bankIntegration.submitFinancingApplication(orderId);
        } catch (RuntimeException e) {
            // A2: błąd walidacji po stronie banku / niekompletne dane -> wniosek do poprawy w CRM.
            this.eventPublisher.publish(new FinancingApplicationFailedEvent(orderId, e.getMessage()));
        }
    }

    // ===== UC-FIN-02: przetworzenie decyzji banku =====

    @Override
    public void processBankDecision(String orderId, boolean approved) {
        FinancingApplication application = this.applicationRepository
                .findByOrderId(new OrderId(orderId))
                .orElseThrow(() -> new FinancingApplicationNotFoundException(
                        "Brak wniosku o finansowanie dla zamówienia " + orderId));

        if (approved) {
            application.approve();                           // PENDING -> APPROVED
            this.applicationRepository.save(application);
            this.eventPublisher.publish(new FinancingApprovedEvent(orderId));
        } else {
            application.reject();                            // PENDING -> REJECTED
            this.applicationRepository.save(application);
            this.eventPublisher.publish(new FinancingRejectedEvent(orderId));
        }
    }
}
