package salon.billing.application.port.out;

import salon.billing.application.domain.model.document.AccountingDocument;
import salon.billing.application.domain.model.document.DocumentId;
import salon.common.model.OrderId;

import java.util.List;
import java.util.Optional;

/**
 * OUTBOUND PORT (Fig. 48 — DocumentDatabaseRepository) — persistence of the Accounting Document aggregate.
 */
public interface DocumentDatabaseRepository {

    void save(AccountingDocument document);

    Optional<AccountingDocument> findById(DocumentId id);

    List<AccountingDocument> findByOrderId(OrderId orderId);
}
