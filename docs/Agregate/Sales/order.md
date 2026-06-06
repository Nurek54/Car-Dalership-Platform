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

        %% Creation of Order from an Offer is handled by a dedicated
        %% factory. See `OrderFactory` in the domain/application layer.
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

    %% OrderFactory: responsibility for constructing valid Order
    %% instances from Offer snapshots/IDs. The factory extracts only
    %% immutable value objects from the Offer and uses them to create
    %% a new Order, avoiding passing aggregate references across roots.
    %% class OrderFactory {
    %%    <<Factory>>
    %%    +createFromOffer(OfferId offerId, OfferSnapshot snapshot) Order
    %%}
