package salon.financing.application.service;

import salon.financing.application.port.in.ProcessFinancing;
import salon.financing.application.port.in.IssueDecisionUseCase;
import salon.financing.application.port.out.BankIntegrationAcl;
import salon.financing.application.domain.exception.BankValidationException;
import salon.financing.application.port.out.SalesIntegration;
import salon.financing.application.port.out.FinancingApplicationDatabaseRepository;
import salon.financing.application.domain.event.FinancingApplicationFailed;
import salon.financing.application.domain.model.financing.ApplicationId;
import salon.financing.application.domain.model.financing.BuyerDetails;
import salon.financing.application.domain.model.financing.CustomerId;
import salon.financing.application.domain.model.financing.FinancingApplication;
import salon.financing.application.domain.model.financing.FinancingApplicationFactory;
import salon.common.application.EventPublisher;
import salon.common.event.DomainEvent;
import salon.common.model.Money;
import salon.common.model.OrderId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Usługa aplikacyjna Kontekstu Finansowania — węzeł "ProcessFinancingService"
 * w docs/Architecture/FinancingArchitecture.md (PDF rozdz. 3.6.3).
 *
 * Realizuje ProcessFinancing (UC-FIN-01) oraz IssueDecisionUseCase (UC-FIN-02).
 * Kreację agregatu deleguje do FinancingApplicationFactory; komunikację z bankiem prowadzi
 * przez dwukierunkowy BankIntegrationAcl. Po wysyłce wniosku proces usypia — wybudza
 * go asynchroniczna decyzja banku (processDecision). Ewaluacja decyzji odbywa się wewnątrz
 * agregatu, a usługa publikuje zdarzenia WYGENEROWANE PRZEZ AGREGAT (FinancingApproved/Rejected).
 */
public class ProcessFinancingService implements ProcessFinancing, IssueDecisionUseCase {

    private final FinancingApplicationDatabaseRepository financingRepository;
    private final FinancingApplicationFactory applicationFactory;
    private final SalesIntegration crmIntegration;
    private final BankIntegrationAcl bankAcl;
    private final EventPublisher eventPublisher;

    public ProcessFinancingService(FinancingApplicationDatabaseRepository financingRepository,
                               FinancingApplicationFactory applicationFactory,
                               SalesIntegration crmIntegration,
                               BankIntegrationAcl bankAcl,
                               EventPublisher eventPublisher) {
        if (financingRepository == null) {
            throw new IllegalArgumentException("financingRepository must not be null.");
        }
        if (applicationFactory == null) {
            throw new IllegalArgumentException("applicationFactory must not be null.");
        }
        if (crmIntegration == null) {
            throw new IllegalArgumentException("crmIntegration must not be null.");
        }
        if (bankAcl == null) {
            throw new IllegalArgumentException("bankAcl must not be null.");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        this.financingRepository = financingRepository;
        this.applicationFactory = applicationFactory;
        this.crmIntegration = crmIntegration;
        this.bankAcl = bankAcl;
        this.eventPublisher = eventPublisher;
    }

    /**
     * UC-FIN-01: złożenie wniosku o finansowanie. Scenariusz A2 (odrzucenie walidacji
     * po stronie banku) kończy się emisją FinancingApplicationFailed — Handlowiec
     * poprawia wniosek wraz z Klientem.
     */
    @Override
    public String submitFinancing(String orderId, String customerId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        OrderId order = new OrderId(orderId);
        // ACL: dane nabywcy i cena końcowa oferty zaciągane z Kontekstu Sprzedaży (CRM).
        BuyerDetails buyerDetails = crmIntegration.getBuyerDetails(order);
        Money moneyForFunding = crmIntegration.getOfferFinalPrice(order);

        FinancingApplication application =
                applicationFactory.createFor(order, new CustomerId(customerId), buyerDetails, moneyForFunding);

        application.submitApplication();
        financingRepository.save(application);

        try {
            bankAcl.submitApplication(application.getId().value(), customerId);
        } catch (BankValidationException e) {
            // A2: bank odrzuca walidację (np. błędny NIP).
            eventPublisher.publish(new FinancingApplicationFailed(
                    UUID.randomUUID(), orderId, e.getMessage(), Instant.now()));
            return application.getId().value();
        }
        return application.getId().value();
    }

    /** UC-FIN-02: przetworzenie asynchronicznej decyzji banku (wybudzenie procesu). */
    @Override
    public void processDecision(String applicationId) {
        if (applicationId == null || applicationId.isBlank()) {
            throw new IllegalArgumentException("applicationId must not be blank.");
        }
        Optional<FinancingApplication> found = financingRepository.findById(new ApplicationId(applicationId));
        if (found.isEmpty()) {
            throw new IllegalStateException("Financing application not found: " + applicationId);
        }
        FinancingApplication application = found.get();

        if (bankAcl.isApproved(applicationId)) {
            application.approve();
        } else {
            application.reject();
        }
        financingRepository.save(application);
        publishEventsOf(application);
    }

    private void publishEventsOf(FinancingApplication application) {
        List<DomainEvent> events = application.pullDomainEvents();
        for (int i = 0; i < events.size(); i++) {
            eventPublisher.publish(events.get(i));
        }
    }
}
