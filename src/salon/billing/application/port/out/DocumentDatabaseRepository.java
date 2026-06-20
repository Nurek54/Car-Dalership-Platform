package salon.billing.application.port.out;

import salon.billing.application.domain.model.document.AccountingDocument;
import salon.billing.application.domain.model.document.DocumentId;
import salon.common.model.OrderId;

import java.util.List;
import java.util.Optional;

/**
 * PORT WYJSCIOWY (Rys. 48 — DocumentDatabaseRepository) — utrwalanie agregatu Dokumentu Ksiegowego.
 */
public interface DocumentDatabaseRepository {

    void save(AccountingDocument document);

    Optional<AccountingDocument> findById(DocumentId id);

    List<AccountingDocument> findByOrderId(OrderId orderId);
}
