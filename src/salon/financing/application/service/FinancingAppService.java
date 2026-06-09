package salon.financing.application.service;

import salon.financing.application.port.in.ProcessFinancingUseCase;
import salon.financing.application.port.out.BankIntegrationAclPort;
import salon.financing.application.port.out.FinancingRepository;
import salon.financing.domain.model.financing.ApplicationId;
import salon.financing.domain.model.financing.CustomerId;
import salon.financing.domain.model.financing.FinancingApplication;
import salon.financing.domain.model.financing.FinancingApplicationFactory;
import salon.shared.application.EventPublisherPort;
import salon.shared.event.DomainEvent;
import salon.shared.model.OrderId;

import java.util.List;
import java.util.Optional;

/**
 * Orkiestracja UC-FIN-01. Reguła "jakie zdarzenie" (Approved/Rejected) jest w agregacie;
 * serwis aplikacyjny tłumaczy decyzję ACL na wywołanie approve()/reject().
 */
public class FinancingAppService implements ProcessFinancingUseCase {

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

    @Override
    public String submitFinancing(String orderId, String customerId) {
        if (orderId == null || orderId.isBlank()) {
            throw new IllegalArgumentException("orderId must not be blank.");
        }
        FinancingApplication application =
                applicationFactory.createFor(new OrderId(orderId), new CustomerId(customerId));

        application.submitApplication();
        financingRepository.save(application);

        bankAcl.submitApplication(application.getId().value(), customerId);
        return application.getId().value();
    }

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
