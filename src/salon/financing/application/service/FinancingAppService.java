package salon.financing.application.service;

import salon.financing.application.port.in.FinancingRequestUseCase;
import salon.financing.application.port.out.BankIntegrationAclPort;
import salon.financing.application.BankValidationException;
import salon.financing.application.port.out.FinancingRepository;
import salon.financing.domain.event.FinancingApplicationFailed;
import salon.financing.domain.model.financing.ApplicationId;
import salon.financing.domain.model.financing.CustomerId;
import salon.financing.domain.model.financing.FinancingApplication;
import salon.financing.domain.model.financing.FinancingApplicationFactory;
import salon.shared.application.EventPublisherPort;
import salon.shared.event.DomainEvent;
import salon.shared.model.OrderId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Usługa aplikacyjna Kontekstu Finansowania — węzeł "FinancingAppService"
 * w docs/Architecture/FinancingArchitecture.md (PDF rozdz. 3.6.3).
 *
 * Realizuje FinancingRequestUseCase (UC-FIN-01 + UC-FIN-02). Kreację agregatu deleguje
 * do FinancingApplicationFactory; komunikację z bankiem prowadzi przez dwukierunkowy
 * BankIntegrationAclPort. Po wysyłce wniosku proces usypia — wybudza go asynchroniczna
 * decyzja banku (processDecision). Ewaluacja decyzji odbywa się wewnątrz agregatu,
 * a usługa publikuje zdarzenia WYGENEROWANE PRZEZ AGREGAT (FinancingApproved/Rejected).
 */
public class FinancingAppService implements FinancingRequestUseCase {

    private final FinancingRepository financingRepository;
    private final FinancingApplicationFactory applicationFactory;
    private final BankIntegrationAclPort bankAcl;
    private final EventPublisherPort eventPublisher;

    public FinancingAppService(FinancingRepository financingRepository,
                               FinancingApplicationFactory applicationFactory,
                               BankIntegrationAclPort bankAcl,
                               EventPublisherPort eventPublisher) {
        if (financingRepository == null) {
            throw new IllegalArgumentException("financingRepository must not be null.");
        }
        if (applicationFactory == null) {
            throw new IllegalArgumentException("applicationFactory must not be null.");
        }
        if (bankAcl == null) {
            throw new IllegalArgumentException("bankAcl must not be null.");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        this.financingRepository = financingRepository;
        this.applicationFactory = applicationFactory;
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
        FinancingApplication application =
                applicationFactory.createFor(new OrderId(orderId), new CustomerId(customerId));

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
