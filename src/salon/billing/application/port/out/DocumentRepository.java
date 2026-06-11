package salon.billing.application.port.out;

import salon.billing.domain.model.document.AccountingDocument;
import salon.billing.domain.model.document.DocumentId;
import salon.shared.model.OrderId;

import java.util.List;
import java.util.Optional;

// Port wyjściowy: repozytorium agregatu AccountingDocument.
public interface DocumentRepository {

    void save(AccountingDocument document);

    Optional<AccountingDocument> findById(DocumentId id);

    /** Dokumenty wystawione dla danego zamówienia (np. proforma zadatku, faktura końcowa). */
    List<AccountingDocument> findByOrderId(OrderId orderId);

    /** Wszystkie dokumenty — wykorzystywane przez cron monitorujący terminy płatności (WF-FIR-03). */
    List<AccountingDocument> findAll();
}
