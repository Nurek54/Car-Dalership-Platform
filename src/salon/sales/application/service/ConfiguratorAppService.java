package salon.sales.application.service;

import org.springframework.stereotype.Service;
import salon.sales.application.command.StartConfiguratorSessionCommand;
import salon.sales.application.port.in.StartConfiguratorUseCase;
import salon.sales.application.port.out.CustomerRepository;
import salon.sales.domain.event.ConfiguratorSessionInitiatedEvent;
import salon.sales.domain.exception.CustomerNotFoundException;
import salon.shared.application.EventPublisherPort;

import java.time.Instant;
import java.util.UUID;

/**
 * Realizuje UC-CRM-01: uruchomienie sesji konfiguratora dla ZAREJESTROWANEGO klienta.
 *
 * Wariant z weryfikacją tożsamości: zanim sesja zostanie zainicjowana, usługa sprawdza,
 * czy klient istnieje w bazie CRM (existsById) — sesji nie wolno otworzyć dla
 * nieistniejącego identyfikatora. Sprzedaż nie trzyma stanu sesji (byt po stronie
 * Katalogu); tutaj jest tylko inicjacja szansy sprzedaży i emisja zdarzenia.
 */
@Service
public class ConfiguratorAppService implements StartConfiguratorUseCase {

    private final CustomerRepository customerRepository;
    private final EventPublisherPort eventPublisher;

    public ConfiguratorAppService(CustomerRepository customerRepository,
                                  EventPublisherPort eventPublisher) {
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
