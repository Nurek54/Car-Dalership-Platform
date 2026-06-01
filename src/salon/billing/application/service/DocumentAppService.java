package salon.billing.application.service;

import salon.billing.application.port.in.IssueDocumentCommand;
import salon.billing.application.port.in.IssueDocumentUseCase;
import salon.billing.application.port.out.DocumentRepository;
import salon.billing.application.port.out.KsefPort;
import salon.billing.application.port.out.KsefSendResult;
import salon.billing.domain.model.document.AccountingDocument;
import salon.billing.domain.model.document.DocumentId;
import salon.billing.domain.model.document.DocumentLine;
import salon.billing.domain.model.document.LineId;
import salon.billing.domain.model.document.TaxDetails;
import salon.shared.application.EventPublisherPort;
import salon.shared.event.DomainEvent;
import salon.shared.model.Money;

/**
 * Realizuje UC-ROZ-02 (warstwa aplikacji — orkiestracja).
 * Zdarzenie FakturaWystawiona generuje agregat (przy confirmKsefRegistration) — serwis tylko je
 * ściąga i publikuje. Na ścieżce PENDING_KSEF agregat nie rejestruje zdarzenia, więc nic nie leci.
 */
public class DocumentAppService implements IssueDocumentUseCase {

    private final DocumentRepository documentRepository;
    private final KsefPort ksefPort;
    private final EventPublisherPort eventPublisher;

    public DocumentAppService(DocumentRepository documentRepository,
                              KsefPort ksefPort,
                              EventPublisherPort eventPublisher) {
        if (documentRepository == null) {
            throw new IllegalArgumentException("documentRepository must not be null.");
        }
        if (ksefPort == null) {
            throw new IllegalArgumentException("ksefPort must not be null.");
        }
        if (eventPublisher == null) {
            throw new IllegalArgumentException("eventPublisher must not be null.");
        }
        this.documentRepository = documentRepository;
        this.ksefPort = ksefPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    // @Transactional w projekcie ze Springiem.
    public String issueDocument(IssueDocumentCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null.");
        }
        if (command.lines() == null || command.lines().isEmpty()) {
            throw new IllegalArgumentException(
                    "Document requires at least one line — complete the service order data.");
        }

        // 1. Tworzymy pusty dokument (DRAFT), numer księgowy = DocumentId.
        TaxDetails taxDetails = new TaxDetails(command.buyerName(), command.nip());
        AccountingDocument document = new AccountingDocument(
                DocumentId.generate(), command.type(), taxDetails);

        // 2. Dokładamy pozycje, nadając kolejne numery LineId (1, 2, 3, ...).
        long nextLineNumber = 1L;
        for (int i = 0; i < command.lines().size(); i++) {
            IssueDocumentCommand.LineData lineData = command.lines().get(i);
            Money cost = new Money(lineData.costAmount(), lineData.costCurrency());
            DocumentLine line = new DocumentLine(new LineId(nextLineNumber), lineData.description(), cost);
            document.addLineItem(line);
            nextLineNumber = nextLineNumber + 1;
        }

        // Zapis wersji roboczej (DRAFT) — zostaje ślad, nawet gdy KSeF nie odpowie.
        documentRepository.save(document);

        // 4-6. Wysyłka do KSeF (agregat sam rejestruje zdarzenie na ścieżce potwierdzenia).
        KsefSendResult result = ksefPort.send(document);
        if (result.accepted()) {
            document.confirmKsefRegistration(result.ksefReference()); // krok 6
        } else {
            document.markAsKsefPending(); // A1: brak odpowiedzi z KSeF
        }
        documentRepository.save(document);

        // 8. Publikacja zdarzeń wygenerowanych przez agregat — bez "if accepted" po stronie serwisu.
        for (DomainEvent event : document.pullDomainEvents()) {
            eventPublisher.publish(event);
        }

        return document.getId().value();
    }
}
