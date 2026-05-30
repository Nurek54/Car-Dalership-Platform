classDiagram
direction TB
class AccountingDocument {
<<AggregateRoot>>
-DocumentId id
-DocumentType type
-DocumentState state
-Money totalAmount
-TaxDetails taxDetails
+markAsKsefPending() void
+confirmKsefRegistration(String ksefReference) void
}
class DocumentId {
<<ValueObject>>
+UUID value
}
class DocumentType {
<<Enumeration>>
VAT_INVOICE
RECEIPT
ESTIMATE
}
class DocumentState {
<<Enumeration>>
DRAFT
PENDING_KSEF
ISSUED
FAILED
}
class TaxDetails {
<<ValueObject>>
+String buyerName
+String nip
}
class DocumentLine {
<<Entity>>
-LineId id
-String description
-Money cost
}
class LineId {
<<ValueObject>>
+Long value
}
class Money {
<<ValueObject>>
+BigDecimal amount
+String currency
}

    AccountingDocument *-- "1" DocumentId : kompozycja (tożsamość)
    AccountingDocument *-- "1" DocumentType : kompozycja
    AccountingDocument *-- "1" DocumentState : kompozycja
    AccountingDocument *-- "1" Money : kompozycja (totalAmount)
    AccountingDocument *-- "1" TaxDetails : kompozycja
    AccountingDocument *-- "1..*" DocumentLine : kompozycja (encje lokalne)
    DocumentLine *-- "1" LineId : kompozycja
    DocumentLine *-- "1" Money : kompozycja (cost)
