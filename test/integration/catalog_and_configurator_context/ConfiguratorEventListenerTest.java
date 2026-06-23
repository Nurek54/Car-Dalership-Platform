package integration.catalog_and_configurator_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import salon.catalog.application.command.InitiateConfiguratorSessionCommand;
import salon.catalog.application.dto.SpecificationView;
import salon.catalog.application.port.in.BuildSpecification;
import salon.catalog.infrastructure.in.event.ConfiguratorEventListener;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** Integracja adaptera wejściowego EventListener — obsługa zdarzenia InitiateConfiguratorSession. */
@SpringBootTest(classes = ConfiguratorEventListener.class)
class ConfiguratorEventListenerTest {

    @Autowired private ConfiguratorEventListener listener;
    @MockBean private BuildSpecification buildSpecification;

    @Test
    void shouldMapMessageToCommandAndOpenSession() {
        // Z kontekstu Sprzedaży przychodzi zdarzenie inicjacji sesji konfiguratora
        SpecificationView stub = new SpecificationView(
                "SPEC-1", "CAT-1", "DRAFT", BigDecimal.ZERO, "PLN", List.of());
        when(buildSpecification.initiate(argThat(c -> c.modelYear() == 2025))).thenReturn(stub);

        SpecificationView result = listener.onInitiateConfiguratorSession(
                new ConfiguratorEventListener.InitiateConfiguratorSessionMessage(2025));

        // Adapter mapuje wiadomość na komendę i wywołuje port wejściowy BuildSpecification
        verify(buildSpecification).initiate(argThat((InitiateConfiguratorSessionCommand c) -> c.modelYear() == 2025));
        assertThat(result).isEqualTo(stub);
    }
}
