package integration.billing_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import salon.billing.application.domain.model.document.AccountingDocument;
import salon.billing.application.domain.model.document.BuyerDetails;
import salon.billing.application.domain.model.document.SellerDetails;
import salon.billing.application.port.out.DocumentDatabaseRepository;
import salon.billing.infrastructure.out.mock.InMemoryDocumentRepository;
import salon.common.model.Money;
import salon.common.model.OrderId;

import static org.assertj.core.api.Assertions.assertThat;

/** Integracja adaptera persystencji dokumentów księgowych (zapis/odczyt po dokumencie i zamówieniu). */
@SpringBootTest(classes = InMemoryDocumentRepository.class)
class InMemoryDocumentRepositoryTest {

    @Autowired private DocumentDatabaseRepository repository;

    private AccountingDocument invoice() {
        return AccountingDocument.createInvoice(new OrderId("ORD-1"),
                new BuyerDetails("Firma S.A.", "1234563218"),
                new SellerDetails("Salon Sp. z o.o.", "5260000000"),
                Money.of(100000, "PLN"), "Faktura końcowa ORD-1", "FA-Księgowy");
    }

    @Test
    void shouldSaveAndFindDocument() {
        AccountingDocument document = invoice();

        repository.save(document);

        assertThat(repository.findById(document.id())).isPresent();
        assertThat(repository.findByOrderId(new OrderId("ORD-1"))).contains(document);
    }
}
