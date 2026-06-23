package unit.financing_context.factories;

import org.junit.jupiter.api.Test;
import salon.common.model.Money;
import salon.financing.application.domain.model.financing.ApplicationState;
import salon.financing.application.domain.model.financing.BuyerDetails;
import salon.financing.application.domain.model.financing.CustomerId;
import salon.financing.application.domain.model.financing.FinancingApplication;
import salon.financing.application.domain.model.financing.FinancingApplicationFactory;
import salon.financing.application.domain.model.financing.OrderId;

import static org.assertj.core.api.Assertions.*;

/** Fabryka agregatu FinancingApplication — spójny stan początkowy wniosku (DRAFT). */
class FinancingApplicationFactoryTest {

    private final FinancingApplicationFactory factory = new FinancingApplicationFactory();

    @Test
    void shouldCreateDraftApplication() {
        OrderId orderId = new OrderId("ORD-1");
        CustomerId customerId = new CustomerId("CUST-1");
        BuyerDetails buyer = new BuyerDetails("Jan Kowalski", "1234563218");
        Money amount = Money.of(100000, "PLN");

        // UC-FIN-01: utworzenie nowego wniosku przez fabrykę
        FinancingApplication application = factory.createDraft(orderId, customerId, buyer, amount);

        assertThat(application.applicationId()).isNotNull(); // fabryka generuje identyfikator
        assertThat(application.state()).isEqualTo(ApplicationState.DRAFT);
        assertThat(application.orderId()).isEqualTo(orderId);
        assertThat(application.customerId()).isEqualTo(customerId);
        assertThat(application.buyerDetails()).isEqualTo(buyer);
        assertThat(application.moneyForFunding()).isEqualTo(amount);
    }
}
