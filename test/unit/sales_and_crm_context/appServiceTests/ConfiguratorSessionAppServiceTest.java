package unit.sales_and_crm_context.appServiceTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.sales.application.command.StartConfiguratorSessionCommand;
import salon.sales.application.service.ConfiguratorAppService;
import salon.sales.application.port.out.CustomerDatabaseRepository;
import salon.common.application.EventPublisher;
import salon.sales.application.domain.event.ConfiguratorSessionInitiatedEvent;
import salon.sales.application.domain.exception.CustomerNotFoundException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** UC-CRM-01: Starting a configurator session for the customer. */
@ExtendWith(MockitoExtension.class)
class ConfiguratorSessionAppServiceTest {

    @Mock private CustomerDatabaseRepository customerRepository;
    @Mock private EventPublisher eventPublisher;
    @InjectMocks private ConfiguratorAppService configuratorAppService;

    @Test
    void shouldEmitConfiguratorSessionInitiatedEventAndReturnSessionId() {
        // We have a registered customer in the database
        StartConfiguratorSessionCommand command = new StartConfiguratorSessionCommand("CUST-1", "SALES-7");
        when(customerRepository.existsById("CUST-1")).thenReturn(true);

        // When the salesperson starts a configurator session for this customer
        String sessionId = configuratorAppService.startConfiguratorSession(command);

        // It broadcasts an event that the session has started
        verify(eventPublisher).publish(any(ConfiguratorSessionInitiatedEvent.class));
    }

    @Test
    void shouldFailToStartSessionWhenCustomerDoesNotExist() {
        // Someone tries to start a session for a non-existent customer ID
        StartConfiguratorSessionCommand command = new StartConfiguratorSessionCommand("UNKNOWN-CUST", "SALES-7");
        when(customerRepository.existsById("UNKNOWN-CUST")).thenReturn(false);

        // The application service aborts the process and throws an error
        assertThatThrownBy(() -> configuratorAppService.startConfiguratorSession(command))
                .isInstanceOf(CustomerNotFoundException.class)
                .hasMessageContaining("Customer with ID UNKNOWN-CUST not found");

        // We check that no false event leaked to other systems
        verify(eventPublisher, never()).publish(any());
    }
}