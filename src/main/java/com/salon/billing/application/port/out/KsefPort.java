package main.java.com.salon.billing.application.port.out;

import main.java.com.salon.billing.domain.model.document.AccountingDocument;

// Port wyjściowy do Krajowego Systemu e-Faktur (KSeF).
public interface KsefPort {
    KsefSendResult send(AccountingDocument document);
}