classDiagram
direction TB

    %% ==========================================
    %% AGREGAT: FINANCING APPLICATION (Wniosek)
    %% ==========================================
    class FinancingApplication {
        <<AggregateRoot>>
        -ApplicationId applicationId
        -OrderId orderId
        -CustomerId customerId
        -ApplicationState state
        +submitApplication() void
        +approve() void
        +reject() void
    }
    class OrderId { <<ValueObject>> }
    class CustomerId { <<ValueObject>> }
    class ApplicationState {
        <<Enumeration>>
        DRAFT
        PENDING
        APPROVED
        REJECTED
    }

    FinancingApplication *-- "1" OrderId : referencja
    FinancingApplication *-- "1" CustomerId : referencja
    FinancingApplication *-- "1" ApplicationState
