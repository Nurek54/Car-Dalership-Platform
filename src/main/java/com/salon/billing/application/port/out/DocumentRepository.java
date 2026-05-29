package main.java.com.salon.billing.application.port.out;

import main.java.com.salon.billing.domain.model.document.AccountingDocument;
import main.java.com.salon.billing.domain.model.document.DocumentId;

import java.util.Optional;

public interface DocumentRepository {
    void save(AccountingDocument document);
    Optional<AccountingDocument> findById(DocumentId id);
}