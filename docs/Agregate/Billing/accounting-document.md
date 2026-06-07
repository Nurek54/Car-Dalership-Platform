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
<<ValueObject>>
-String description
-Money cost
}
class Money {
<<ValueObject>>
+BigDecimal amount
+String currency
}

    AccountingDocument *-- "1" DocumentId
    AccountingDocument *-- "1" DocumentType
    AccountingDocument *-- "1" DocumentState
    AccountingDocument *-- "1" Money
    AccountingDocument *-- "1" TaxDetails
    AccountingDocument *-- "1..*" DocumentLine
    DocumentLine *-- "1" Money

    %% NOTE: DocumentLine is modelled as a Value Object. The persistence
    %% layer may generate a surrogate `LineId`, but this identifier is an
    %% infrastructure concern and must not leak into the domain model.

    %% Factory for creating AccountingDocument instances from validated
    %% input snapshots; factory ensures all invariants and composes
    %% value objects (lines, amounts, tax details) before returning the
    %% fully-initialised aggregate root.
    %% class AccountingDocumentFactory {
    %%    <<Factory>>
    %%    +createIssue(DocumentSnapshot snapshot) AccountingDocument
    %%}
