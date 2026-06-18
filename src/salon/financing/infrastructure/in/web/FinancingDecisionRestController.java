package salon.financing.infrastructure.in.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import salon.financing.application.port.in.IssueDecisionUseCase;

/**
 * Adapter sterujacy (driving) — REST API decyzji finansowej (UC-FIN-02), PDF rozdz. 3.6.3
 * ("FinancingDecisionRestController"). Udostepnia interfejs analitykowi/pracownikowi banku.
 *
 * Cienki: parsuje zadanie sieciowe i deleguje do portu wejsciowego IssueDecisionUseCase.
 */
@RestController
@RequestMapping("/api/financing/decisions")
public class FinancingDecisionRestController {

    private final IssueDecisionUseCase issueDecision;

    public FinancingDecisionRestController(IssueDecisionUseCase issueDecision) {
        if (issueDecision == null) {
            throw new IllegalArgumentException("issueDecision must not be null.");
        }
        this.issueDecision = issueDecision;
    }

    /** UC-FIN-02: przetworzenie decyzji banku dla wskazanego wniosku. */
    @PostMapping("/{applicationId}")
    public ResponseEntity<Void> issueDecision(@PathVariable String applicationId) {
        issueDecision.processDecision(applicationId);
        return ResponseEntity.ok().build();
    }
}
