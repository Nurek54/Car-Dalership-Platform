package salon.financing.application.service;

import salon.financing.application.port.in.IssuePolicyUseCase;
import salon.financing.application.port.out.InsurerIntegrationAclPort;
import salon.financing.application.port.out.PolicyRepository;
import salon.financing.domain.model.insurance.InsurancePolicy;
import salon.financing.domain.model.insurance.PolicyId;
import salon.financing.domain.model.insurance.PolicyType;
import salon.financing.domain.model.insurance.VinNumber;
import salon.financing.domain.service.ResidualValueCalculationService;
import salon.shared.application.EventPublisherPort;
import salon.shared.event.DomainEvent;
import salon.shared.model.Money;

import java.math.BigDecimal;
import java.util.List;

/**
 * Orkiestracja UC-FIN-02. Wartość rezydualną liczy serwis dziedziny, aktywację — agregat.
 */
public class InsuranceAppService implements IssuePolicyUseCase {

    private final PolicyRepository policyRepository;
    private final InsurerIntegrationAclPort insurerAcl;
    private final ResidualValueCalculationService residualValueService;
    private final EventPublisherPort eventPublisher;

    public InsuranceAppService(PolicyRepository policyRepository,
                               InsurerIntegrationAclPort insurerAcl,
                               ResidualValueCalculationService residualValueService,
                               EventPublisherPort eventPublisher) {
        if (policyRepository == null) {
            throw new IllegalArgumentException("policyRepository must not be null.");
        }
        if (insurerAcl == null) {
            throw new IllegalArgumentException("insurerAcl must not be null.");
        }
        if (residualValueService == null) {
            throw new IllegalArgumentException("residualValueService must not be null.");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        this.policyRepository = policyRepository;
        this.insurerAcl = insurerAcl;
        this.residualValueService = residualValueService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public String issuePolicy(String vin, String policyType, BigDecimal basePrice, String currency, int ageInYears) {
        if (vin == null || vin.isBlank()) {
            throw new IllegalArgumentException("vin must not be blank.");
        }
        VinNumber vinNumber = new VinNumber(vin);
        PolicyType type = PolicyType.valueOf(policyType);

        InsurancePolicy policy = new InsurancePolicy(PolicyId.generate(), vinNumber, type);

        Money residual = residualValueService.calculateResidualValue(new Money(basePrice, currency), ageInYears);
        policy.assignResidualValue(residual);

        String insurerReference = insurerAcl.issuePolicy(vin, residual.getAmount());
        policy.activatePolicy(insurerReference);

        policyRepository.save(policy);
        publishEventsOf(policy);
        return policy.getId().value();
    }

    private void publishEventsOf(InsurancePolicy policy) {
        List<DomainEvent> events = policy.pullDomainEvents();
        for (int i = 0; i < events.size(); i++) {
            eventPublisher.publish(events.get(i));
        }
    }
}
