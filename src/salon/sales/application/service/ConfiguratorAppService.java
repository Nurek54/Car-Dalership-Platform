package salon.sales.application.service;

import salon.sales.application.port.in.StartConfiguratorSessionCommand;
import salon.sales.application.port.in.StartConfiguratorSessionUseCase;
import salon.sales.domain.event.ConfiguratorSessionInitiatedEvent;
import salon.shared.application.EventPublisherPort;

import java.time.Instant;
import java.util.UUID;

/**
 * Realizuje UC-CRM-01: uruchomienie sesji konfiguratora dla klienta.
 *
 * Generuje identyfikator sesji i emituje zdarzenie {@link ConfiguratorSessionInitiatedEvent}
 * (InitiateConfiguratorSession), które otwiera konfigurator w Kontekście Katalogu. Sprzedaż
 * nie trzyma stanu sesji — to byt po stronie Katalogu; tutaj jest tylko inicjacja szansy sprzedaży.
 */
public class ConfiguratorAppService implements StartConfiguratorSessionUseCase {

    private final EventPublisherPort eventPublisher;

    public ConfiguratorAppService(EventPublisherPort eventPublisher) {
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        this.eventPublisher = eventPublisher;
    }

    @Override
    public String startConfiguratorSession(StartConfiguratorSessionCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null.");
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
