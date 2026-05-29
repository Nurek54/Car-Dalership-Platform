package main.java.com.salon.billing.application.port.in;

import java.util.UUID;

// Port wejściowy dla UC-ROZ-02. Zwraca id (= numer) utworzonego dokumentu.
public interface IssueDocumentUseCase {
    UUID issueDocument(IssueDocumentCommand command);
}