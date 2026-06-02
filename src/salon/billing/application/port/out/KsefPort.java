package salon.billing.application.port.out;

import salon.billing.domain.model.document.AccountingDocument;

// Port wyjściowy do Krajowego Systemu e-Faktur (KSeF).
public interface KsefPort {
    KsefSendResult send(AccountingDocument document);
}
