package unit.sales_and_crm_context;

import org.junit.jupiter.api.Test;
import salon.sales.application.port.in.StartConfiguratorSessionCommand;
import salon.sales.application.service.ConfiguratorAppService;
import salon.sales.domain.event.ConfiguratorSessionInitiatedEvent;
import salon.shared.application.EventPublisherPort;
import salon.shared.event.DomainEvent;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/** UC-CRM-01: uruchomienie sesji konfiguratora dla klienta. */
class ConfiguratorSessionTest {

    @Test
    void shouldEmitConfiguratorSessionInitiatedEventAndReturnSessionId() {
        List<DomainEvent> published = new ArrayList<>();
        EventPublisherPort publisher = published::add;
        ConfiguratorAppService service = new ConfiguratorAppService(publisher);

        String sessionId = service.startConfiguratorSession(
                new StartConfiguratorSessionCommand("CUST-1", "SALES-7"));

        assertThat(sessionId).startsWith("CFG-");
        assertThat(published).hasSize(1);
        assertThat(published.get(0)).isInstanceOf(ConfiguratorSessionInitiatedEvent.class);
        ConfiguratorSessionInitiatedEvent ev = (ConfiguratorSessionInitiatedEvent) published.get(0);
        assertThat(ev.customerId()).isEqualTo("CUST-1");
        assertThat(ev.salespersonId()).isEqualTo("SALES-7");
        assertThat(ev.sessionId()).isEqualTo(sessionId);
    }
}
