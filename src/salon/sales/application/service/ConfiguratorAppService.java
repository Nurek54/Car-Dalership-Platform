package salon.sales.application.service;

import org.springframework.stereotype.Service;
import salon.sales.application.command.StartConfiguratorSessionCommand;
import salon.sales.application.port.in.StartConfigurator;
import salon.sales.application.port.out.CustomerDatabaseRepository;
import salon.sales.application.domain.event.ConfiguratorSessionInitiatedEvent;
import salon.sales.application.domain.exception.CustomerNotFoundException;
import salon.common.application.EventPublisher;

import java.time.Instant;
import java.util.UUID;

/**
 * Implements UC-CRM-01: starting a configurator session for a REGISTERED customer.
 *
 * Variant with identity verification: before the session is initiated, the service checks
 * whether the customer exists in the CRM database (existsById) — the session must not be opened for
 * a non-existent identifier. Sales does not hold the session state (the entity is on the
 * Catalog side); here there is only the initiation of the sales opportunity and the event emission.
 */
@Service
public class ConfiguratorAppService implements StartConfigurator {

    private final CustomerDatabaseRepository customerRepository;
    private final EventPublisher eventPublisher;

    public ConfiguratorAppService(CustomerDatabaseRepository customerRepository,
                                  EventPublisher eventPublisher) {
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public String startConfiguratorSession(StartConfiguratorSessionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null.");
        }
        if (!customerRepository.existsById(command.customerId())) {
            throw new CustomerNotFoundException(
                    "Customer with ID " + command.customerId() + " not found");
        }
        String sessionId = "CFG-" + UUID.randomUUID();
        eventPublisher.publish(new ConfiguratorSessionInitiatedEvent(
                UUID.randomUUID(),
                sessionId,
                command.customerId(),
                command.salespersonId(),
                Instant.now()));
        return sessionId;
    }
}
