classDiagram
direction TB

    %% ==========================================
    %% AGREGAT: FINANCING APPLICATION (Wniosek)
    %% ==========================================
    class FinancingApplication {
        <<AggregateRoot>>
        -ApplicationId id
        -OrderId orderId
        -CustomerId customerId
        -Money requestedAmount
        -ApplicationState state
        -FinancingDecision bankDecision
        +submitApplication() void
        +processBankDecision(FinancingDecision decision) void
    }
    class ApplicationId { <<ValueObject>> }
    class OrderId { <<ValueObject>> }
    class CustomerId { <<ValueObject>> }
    class Money { <<ValueObject>> }
    class FinancingDecision {
        <<ValueObject>>
        +String bankReference
        +BigDecimal grantedAmount
        +DecisionStatus status
    }
    class ApplicationState {
        <<Enumeration>>
        DRAFT
        SUBMITTED_TO_BANK
        APPROVED
        REJECTED
    }

    FinancingApplication *-- "1" ApplicationId
    FinancingApplication *-- "1" OrderId : referencja
    FinancingApplication *-- "1" CustomerId : referencja
    FinancingApplication *-- "1" Money
    FinancingApplication *-- "1" ApplicationState
    FinancingApplication *-- "0..1" FinancingDecision : pojawia się po decyzji banku
