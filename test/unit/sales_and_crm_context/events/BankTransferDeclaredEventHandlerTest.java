package unit.sales_and_crm_context.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.sales.application.handler.BankTransferDeclaredEventHandler;
import salon.sales.application.port.out.BillingIntegration;
import salon.sales.application.domain.event.BankTransferDeclaredEvent;
import salon.common.model.Money;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankTransferDeclaredEventHandlerTest {

    @Mock private BillingIntegration billingPort;
    @InjectMocks private BankTransferDeclaredEventHandler eventHandler;

    @Test
    void shouldRequestProformaInvoiceWhenBankTransferIsDeclared() {
        // Klient zadeklarował przelew
        Money declaredAmount = Money.of(150000, "PLN");
        BankTransferDeclaredEvent event = new BankTransferDeclaredEvent(
                UUID.randomUUID(),
                "ORD-500",
                declaredAmount,
                Instant.now()
        );

        eventHandler.handle(event);

        // Zlecamy do działu Rozliczeń wystawienie dokumentu proforma
        verify(billingPort).requestProformaInvoice("ORD-500", declaredAmount);
    }
}