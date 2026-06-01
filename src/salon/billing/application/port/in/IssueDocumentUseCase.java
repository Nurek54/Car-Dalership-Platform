package salon.billing.application.port.in;

// Port wejściowy dla UC-ROZ-02. Zwraca id (= numer) utworzonego dokumentu.
public interface IssueDocumentUseCase {
    String issueDocument(IssueDocumentCommand command);
}
