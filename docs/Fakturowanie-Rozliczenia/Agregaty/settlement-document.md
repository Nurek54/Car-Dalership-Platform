classDiagram
direction TB

    %% ---------------------------------------------
    %% AGREGAT 1: SETTLEMENT (ROZLICZENIE / SALDO)
    %% ---------------------------------------------
    class Settlement {
        <<AggregateRoot>>
        -SettlementId id
        -OrderId orderId
        -Money totalAmount
        -List~Payment~ payments
        -SettlementStatus status
        +requestAdvancePayment() void
        +registerPayment(String transactionId, Money amount) void
        -recalculateBalance() void
    }

    class Payment {
        <<Entity>>
        -String transactionId
        -Money amount
        -LocalDateTime paymentDate
    }

    class SettlementStatus {
        <<Enumeration>>
        OPEN
        PARTIAL_PAYMENT
        SETTLED
    }

    Settlement *-- "0..*" Payment
    Settlement *-- "1" SettlementStatus

    %% ---------------------------------------------
    %% AGREGAT 2: ACCOUNTING DOCUMENT (FAKTURA)
    %% ---------------------------------------------
    class AccountingDocument {
        <<AggregateRoot>>
        -DocumentId id
        -OrderId orderId
        -String invoiceTitle
        -BuyerDetails buyer
        -SellerDetails seller
        -Money totalAmount
        -LocalDate issueDate
        -LocalDate dueDate
        -String authorizedIssuer
        -DocumentStatus status
        +createInvoice(OrderId, BuyerDetails, SellerDetails, Money, String, String)$ AccountingDocument
        +generatePdf() byte[]
        +markAsIssued() void
    }

    class BuyerDetails {
        <<ValueObject>>
        +String name
        +String nip
        +isCorporate() boolean
    }

    class SellerDetails {
        <<ValueObject>>
        +String name
        +String nip
    }

    class DocumentStatus {
        <<Enumeration>>
        DRAFT
        ISSUED
        ERROR
    }

    AccountingDocument *-- "1" BuyerDetails
    AccountingDocument *-- "1" SellerDetails
    AccountingDocument *-- "1" DocumentStatus
