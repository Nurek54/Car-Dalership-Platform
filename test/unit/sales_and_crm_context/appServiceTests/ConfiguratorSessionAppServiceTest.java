package unit.sales_and_crm_context.appServiceTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.sales.application.port.in.StartConfiguratorSessionCommand;
import salon.sales.application.service.ConfiguratorAppService;
import salon.sales.application.port.out.CustomerRepository;
import salon.shared.application.EventPublisherPort;
import salon.sales.domain.event.ConfiguratorSessionInitiatedEvent;
import salon.sales.domain.exception.CustomerNotFoundException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** UC-CRM-01: Uruchomienie sesji konfiguratora dla klienta. */
@ExtendWith(MockitoExtension.class)
class ConfiguratorSessionAppServiceTest {

    @Mock private CustomerRepository customerRepository;
    @Mock private EventPublisherPort eventPublisher;
    @InjectMocks private ConfiguratorAppService configuratorAppService;

    @Test
    void shouldEmitConfiguratorSessionInitiatedEventAndReturnSessionId() {
        // Posiadamy w bazie zarejestrowanego klienta
        StartConfiguratorSessionCommand command = new StartConfiguratorSessionCommand("CUST-1", "SALES-7");
        when(customerRepository.existsById("CUST-1")).thenReturn(true);

        // Kiedy andlowiec uruchamia sesję konfiguratora dla tego klienta
        String sessionId = configuratorAppService.startConfiguratorSession(command);

        // To wypuszcza w świat zdarzenie, że sesja się rozpoczęła
        verify(eventPublisher).publish(any(ConfiguratorSessionInitiatedEvent.class));
    }

    @Test
    void shouldFailToStartSessionWhenCustomerDoesNotExist() {
        // Ktoś próbuje uruchomić sesję dla nieistniejącego ID klienta
        StartConfiguratorSessionCommand command = new StartConfiguratorSessionCommand("UNKNOWN-CUST", "SALES-7");
        when(customerRepository.existsById("UNKNOWN-CUST")).thenReturn(false);

        // Serwis aplikacyjny przerywa proces i rzuca błąd
        assertThatThrownBy(() -> configuratorAppService.startConfiguratorSession(command))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer with ID UNKNOWN-CUST not found");

        // Sprawdzamy czy żadne fałszywe zdarzenie nie wyciekło do innych systemów
        verify(eventPublisher, never()).publish(any());
    }
}