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
 * APPLICATION SERVICE (Figure 42) – "ProcessFinancingService".
 *
 * The single orchestration point of the Financing Context. Implements the inbound port {@link ProcessFinancing}
 * (UC-FIN-01/02), using the outbound ports: {@link FinancingApplicationDatabaseRepository},
 * {@link SalesIntegration}, {@link BankIntegrationAcl} and the shared {@link EventPublisher}.
 * The dealership does not make the credit decision — the service only sends the application and reflects the bank's decision.
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

    // ===== UC-FIN-01: submitting the financing application =====

    @Override
    public void requestFinancing(String orderId, String customerId) {
        try {
            // Step 2: aggregating data from the Sales Context (buyer data + amount to be financed).
            BuyerDetails buyerDetails = this.salesIntegration.buyerDetails(orderId);
            Money amountToFinance = this.salesIntegration.offerFinalPrice(orderId);

            // Step 3: building the local application (DRAFT) — translation into the domain model.
            FinancingApplication application = this.applicationFactory.createDraft(
                    new OrderId(orderId), new CustomerId(customerId), buyerDetails, amountToFinance);

            // Steps 4–5: sending to the bank (ACL) and marking "Under bank verification".
            application.submitApplication();                 // DRAFT -> PENDING (rule in the aggregate)
            this.applicationRepository.save(application);
            this.bankIntegration.submitFinancingApplication(orderId);
        } catch (RuntimeException e) {
            // A2: a validation error on the bank side / incomplete data -> the application goes back for correction in CRM.
            this.eventPublisher.publish(new FinancingApplicationFailedEvent(orderId, e.getMessage()));
        }
    }

    // ===== UC-FIN-02: processing the bank's decision =====

    @Override
    public void processBankDecision(String orderId, boolean approved) {
        FinancingApplication application = this.applicationRepository
                .findByOrderId(new OrderId(orderId))
                .orElseThrow(() -> new FinancingApplicationNotFoundException(
                        "No financing application for order " + orderId));

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
