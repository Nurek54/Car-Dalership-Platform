package salon.catalog.infrastructure.in.event;

import salon.catalog.application.dto.InitiateConfiguratorSessionCommand;
import salon.catalog.application.dto.SpecificationView;
import salon.catalog.application.port.in.BuildSpecification;
import org.springframework.stereotype.Component;

/**
 * ADAPTER WEJŚCIOWY (sterowany zdarzeniami) – „EventListener” z diagramu.
 *
 * Nasłuchuje zdarzenia integracyjnego InitiateConfiguratorSession (z kontekstu
 * „Sprzedaż i CRM”, mapowanie OHS) i wywołuje port wejściowy {@link BuildSpecification}.
 * Zadania adaptera: mapowanie komunikatu na model danych wejściowych usługi aplikacji
 * i wywołanie fasady przypadku użycia. Nie zawiera logiki biznesowej.
 *
 * W realnej integracji metoda zostałaby oznaczona np. @RabbitListener(queues = "...").
 */
@Component
public class ConfiguratorEventListener {

    private final BuildSpecification buildSpecification;

    public ConfiguratorEventListener(BuildSpecification buildSpecification) {
        this.buildSpecification = buildSpecification;
    }

    /**
     * Obsługa zdarzenia InitiateConfiguratorSession.
     *
     * @return widok utworzonej (roboczej) specyfikacji – identyfikator sesji dla UI
     */
    public SpecificationView onInitiateConfiguratorSession(InitiateConfiguratorSessionMessage message) {
        InitiateConfiguratorSessionCommand command =
                new InitiateConfiguratorSessionCommand(message.modelYear());
        return buildSpecification.initiate(command);
    }

    /** Treść zdarzenia integracyjnego (minimalny zbiór informacji). */
    public record InitiateConfiguratorSessionMessage(int modelYear) {
    }
}
