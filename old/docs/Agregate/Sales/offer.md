classDiagram
direction TB

    %% ==========================================
    %% AGREGAT: OFFER (Proforma)
    %% ==========================================
    class Offer {
        <<AggregateRoot>>
        -OfferId id
        -CustomerId customerId
        -SpecificationId specificationId
        -Money basePrice
        -Discount appliedDiscount
        -Money finalPrice
        -Date validityDate
        -OfferState state
        +applyDiscount(Discount discount, DiscountLimit salesRepLimit) void
        +approveDiscountByDirector() void
        +publish() void
        +markAsConverted() void
    }
    class OfferId { <<ValueObject>> }
    class CustomerId { <<ValueObject>> }
    class SpecificationId { <<ValueObject>> }
    class Discount {
        <<ValueObject>>
        +BigDecimal percentage
    }
    class DiscountLimit {
        <<ValueObject>>
        +BigDecimal maxAllowed
    }
    class OfferState {
        <<Enumeration>>
        DRAFT
        PENDING_DIRECTOR_APPROVAL
        PUBLISHED
        EXPIRED
        CONVERTED
    }
    class Money { <<ValueObject>> }

    %% Relacje strukturalne (Kompozycja / Asocjacja) - Agregat przechowuje te dane
    Offer *-- "1" OfferId
    Offer *-- "1" CustomerId : referencja
    Offer *-- "1" SpecificationId : referencja
    Offer *-- "2" Money
    Offer *-- "1" Discount
    Offer *-- "1" OfferState

    %% Relacja behawioralna (Zależność) - Agregat używa tego tylko w metodzie
    Offer ..> "1" DiscountLimit : używa jako argumentu

    %% NOTE: All Value Objects in the model are immutable. The
    %% `applyDiscount(...)` operation uses `DiscountLimit` only as a
    %% read-only predicate/validator. Any state change resulting from
    %% applying a discount must be performed by the aggregate root
    %% `Offer` (e.g. replacing the old `Discount`/`Money` value objects),
    %% never by mutating the value objects themselves.
