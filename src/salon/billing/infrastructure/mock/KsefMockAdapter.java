package salon.billing.infrastructure.mock;

import salon.billing.application.port.out.KsefPort;
import salon.billing.application.port.out.KsefSendResult;
import salon.billing.domain.model.document.AccountingDocument;

// Udawany KSeF: zawsze akceptuje i zwraca przykładowy numer referencyjny.
public class KsefMockAdapter implements KsefPort {

    @Override
    public KsefSendResult send(AccountingDocument document) {
        String reference = "KSEF-MOCK-" + document.getId().value();
        return new KsefSendResult(true, reference);
    }
}
