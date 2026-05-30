classDiagram
direction TB

    %% ==========================================
    %% AGREGAT: ORDER (Zamówienie)
    %% ==========================================
    class Order {
        <<AggregateRoot>>
        -OrderId id
        -OfferId sourceOfferId
        -Money requiredDeposit
        -OrderState state
        -CancellationReason cancellationReason
        +createFromOffer(Offer offer) Order$
        +confirmSignature(String signatureRef) void
        +cancelOrder(CancellationReason reason, boolean isHandedOver) void
    }
    class OrderId { <<ValueObject>> }
    class OfferId { <<ValueObject>> }
    class Money { <<ValueObject>> }
    class CancellationReason {
        <<Enumeration>>
        CLIENT_FAULT
        DEALER_FAULT
        NONE
    }
    class OrderState {
        <<Enumeration>>
        DRAFT_CREATED
        PENDING_PAYMENT
        IN_PROGRESS
        CANCELLATION_STARTED
        CANCELLED
        COMPLETED
    }

    Order *-- "1" OrderId
    Order *-- "1" OfferId : referencja
    Order *-- "1" Money
    Order *-- "1" OrderState
    Order *-- "1" CancellationReason
