package unit.sales_and_crm_context.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.sales.application.handler.ConfiguratorSessionInitiatedEventHandler;
import salon.sales.application.port.out.CatalogIntegration;
import salon.sales.application.domain.event.ConfiguratorSessionInitiatedEvent;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConfiguratorSessionInitiatedEventHandlerTest {

    @Mock private CatalogIntegration catalogPort;
    @InjectMocks private ConfiguratorSessionInitiatedEventHandler eventHandler;

    @Test
    void shouldNotifyCatalogContextToOpenInterface() {
        ConfiguratorSessionInitiatedEvent event = new ConfiguratorSessionInitiatedEvent(
                UUID.randomUUID(),
                "SESSION-999",
                "CUST-1",
                "SALES-7",
                Instant.now()
        );

        eventHandler.handle(event);

        // Zlecamy portowi Katalogu otwarcie interfejsu
        verify(catalogPort).openConfiguratorInterface("SESSION-999", "CUST-1", "SALES-7");
    }
}