package salon.sales.application.service;

import org.springframework.stereotype.Service;

import salon.common.application.EventPublisher;
import salon.sales.application.command.StartConfiguratorSessionCommand;
import salon.sales.application.domain.event.ConfiguratorSessionInitiatedEvent;
import salon.sales.application.domain.exception.CustomerNotFoundException;
import salon.sales.application.port.in.StartConfigurator;
import salon.sales.application.port.out.CustomerDatabaseRepository;

import java.util.UUID;

@Service
public class ConfiguratorAppService implements StartConfigurator {

    private final CustomerDatabaseRepository customerRepository;
    private final EventPublisher eventPublisher;

    public ConfiguratorAppService(CustomerDatabaseRepository customerRepository, EventPublisher eventPublisher) {
        if (customerRepository == null) {
            throw new IllegalArgumentException("customerRepository must not be null.");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        this.customerRepository = customerRepository;
        this.eventPublisher = eventPublisher;
    }

    
    public String startConfiguratorSession(StartConfiguratorSessionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null.");
        }
        if (!this.customerRepository.existsById(command.customerId())) {
            throw new CustomerNotFoundException("Customer with ID " + command.customerId() + " not found");
        }
        String sessionId = "SES-" + UUID.randomUUID();
        this.eventPublisher.publish(new ConfiguratorSessionInitiatedEvent(
                sessionId, command.customerId(), command.salespersonId()));
        return sessionId;
    }

    
    @Override
    public String startSession(StartConfiguratorSessionCommand command) {
        return startConfiguratorSession(command);
    }
}
