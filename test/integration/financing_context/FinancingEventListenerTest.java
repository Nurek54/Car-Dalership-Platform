package integration.financing_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import salon.financing.application.port.in.ProcessFinancing;
import salon.financing.infrastructure.in.messaging.FinancingEventListener;

import static org.mockito.Mockito.verify;

/** UC-FIN-01/02: Adapter nasłuchujący zdarzeń — wnioskowanie i przetwarzanie decyzji banku. */
@SpringBootTest(classes = FinancingEventListener.class)
class FinancingEventListenerTest {

    @Autowired private FinancingEventListener listener;
    @MockBean private ProcessFinancing processFinancing;

    @Test
    void shouldRequestFinancingOnFinancingRequested() { // UC-FIN-01
        // Klient w CRM wybrał finansowanie — przychodzi zdarzenie FinancingRequested
        listener.handleFinancingRequested(
                new FinancingEventListener.FinancingRequested("ORD-1", "CUST-1"));

        verify(processFinancing).requestFinancing("ORD-1", "CUST-1");
    }

    @Test
    void shouldProcessBankDecisionOnDecisionReceived() { // UC-FIN-02
        // Z adaptera bankowego przychodzi asynchroniczna decyzja
        listener.handleFinancingDecisionReceived(
                new FinancingEventListener.FinancingDecisionReceivedFromBank("ORD-2", true));

        verify(processFinancing).processBankDecision("ORD-2", true);
    }
}
