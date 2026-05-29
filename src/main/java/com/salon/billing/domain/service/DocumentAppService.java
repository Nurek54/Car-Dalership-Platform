package main.java.com.salon.billing.domain.service;

import main.java.com.salon.billing.application.port.in.IssueDocumentCommand;
import main.java.com.salon.billing.application.port.in.IssueDocumentUseCase;
import main.java.com.salon.billing.application.port.out.DocumentRepository;
import main.java.com.salon.billing.application.port.out.EventPublisherPort;
import main.java.com.salon.billing.application.port.out.KsefPort;
import main.java.com.salon.billing.application.port.out.KsefSendResult;
import main.java.com.salon.billing.domain.event.InvoiceIssuedEvent;
import main.java.com.salon.billing.domain.model.document.AccountingDocument;
import main.java.com.salon.billing.domain.model.document.DocumentId;
import main.java.com.salon.billing.domain.model.document.DocumentLine;
import main.java.com.salon.billing.domain.model.document.LineId;
import main.java.com.salon.billing.domain.model.document.TaxDetails;
import main.java.com.salon.billing.domain.model.shared.Money;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Realizuje UC-ROZ-02.
 */
public class DocumentAppService implements IssueDocumentUseCase {

    private final DocumentRepository documentRepository;
    private final KsefPort ksefPort;
    private final EventPublisherPort eventPublisher;

    public DocumentAppService(DocumentRepository documentRepository,
                              KsefPort ksefPort,
                              EventPublisherPort eventPublisher) {
        this.documentRepository = documentRepository;
        this.ksefPort = ksefPort;
        this.eventPublisher = eventPublisher;
    }

    @Override
    // @Transactional w projekcie ze Springiem.
    public UUID issueDocument(IssueDocumentCommand command) {
        // A2: brak pozycji (części/roboczogodzin) -> blokujemy wystawienie.
        if (command.lines() == null || command.lines().isEmpty()) {
            throw new IllegalArgumentException(
                    "Dokument wymaga co najmniej jednej pozycji — uzupełnij dane zlecenia.");
        }

        // 2. Budujemy pozycje, nadając kolejne numery LineId (1, 2, 3, ...).
        List<DocumentLine> documentLines = new ArrayList<>();
        long nextLineNumber = 1L;
        for (int i = 0; i < command.lines().size(); i++) {
            IssueDocumentCommand.LineData lineData = command.lines().get(i);
            Money cost = new Money(lineData.costAmount(), lineData.costCurrency());
            DocumentLine line = new DocumentLine(new LineId(nextLineNumber), lineData.description(), cost);
            documentLines.add(line);
            nextLineNumber = nextLineNumber + 1;
        }

        // 3. Tworzymy dokument (numer księgowy = DocumentId).
        TaxDetails taxDetails = new TaxDetails(command.buyerName(), command.nip());
        AccountingDocument document = new AccountingDocument(
                DocumentId.generate(), command.type(), taxDetails, documentLines);

        // Zapis wersji roboczej (DRAFT) — zostaje ślad nawet, gdy KSeF nie odpowie.
        documentRepository.save(document);

        // 4-6. Wysyłka do KSeF.
        KsefSendResult result = ksefPort.send(document);
        if (result.accepted()) {
            document.confirmKsefRegistration(result.ksefReference()); // krok 6
        } else {
            document.markAsKsefPending(); // A1: brak odpowiedzi z KSeF
        }
        documentRepository.save(document);

        // 8. Zdarzenie FakturaWystawiona — tylko gdy dokument faktycznie wystawiono.
        if (result.accepted()) {
            InvoiceIssuedEvent event = new InvoiceIssuedEvent(
                    document.getId().value(),
                    document.getKsefReference(),
                    Instant.now());
            eventPublisher.publish(event);
        }

        // Krok 7 (podgląd PDF) jest poza zakresem tego szkieletu.
        return document.getId().value();
    }
}