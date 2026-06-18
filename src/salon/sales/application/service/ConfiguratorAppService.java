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
 * Realizuje UC-CRM-01: uruchomienie sesji konfiguratora dla ZAREJESTROWANEGO klienta.
 *
 * Wariant z weryfikacją tożsamości: zanim sesja zostanie zainicjowana, usługa sprawdza,
 * czy klient istnieje w bazie CRM (existsById) — sesji nie wolno otworzyć dla
 * nieistniejącego identyfikatora. Sprzedaż nie trzyma stanu sesji (byt po stronie
 * Katalogu); tutaj jest tylko inicjacja szansy sprzedaży i emisja zdarzenia.
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
