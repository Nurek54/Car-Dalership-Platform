package main.java.com.salon.billing.infrastructure.mock;

import main.java.com.salon.billing.application.port.out.KsefPort;
import main.java.com.salon.billing.application.port.out.KsefSendResult;
import main.java.com.salon.billing.domain.model.document.AccountingDocument;

import java.util.UUID;

/**
 * Adapter do KSeF — wersja MOCK.
 * Zawsze "akceptuje" dokument i zwraca losowy numer referencyjny KSeF.
 * Aby przetestować A1 (brak odpowiedzi z KSeF), zwróć new KsefSendResult(false, null).
 */
public class KsefMockAdapter implements KsefPort {

    @Override
    public KsefSendResult send(AccountingDocument document) {
        if (document == null) {
            throw new IllegalArgumentException("document must not be null.");
        }
        // Mock: udajemy poprawną rejestrację w KSeF.
        String ksefReference = "KSEF-MOCK-" + UUID.randomUUID();
        return new KsefSendResult(true, ksefReference);
    }
}
