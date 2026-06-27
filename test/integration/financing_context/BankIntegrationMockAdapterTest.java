package integration.financing_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import salon.financing.application.port.out.BankIntegrationAcl;
import salon.financing.infrastructure.out.mock.BankIntegrationMockAdapter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/** Integracja adaptera ACL banku (asynchroniczne wysłanie wniosku o finansowanie). */
@SpringBootTest(classes = BankIntegrationMockAdapter.class)
class BankIntegrationMockAdapterTest {

    @Autowired private BankIntegrationAcl adapter;

    @Test
    void shouldSubmitFinancingApplicationWithoutError() {
        // Wniosek jest wysyłany do banku — adapter nie zgłasza błędu (decyzja przyjdzie asynchronicznie)
        assertDoesNotThrow(() -> adapter.submitFinancingApplication("ORD-1"));
    }

    @Test
    void shouldStartCreditCheckProcessViaDefaultMethod() {
        // Alias semantyczny używany przez Sprzedaż deleguje do wysłania wniosku
        assertDoesNotThrow(() -> adapter.startCreditCheckProcess("ORD-2"));
    }
}
