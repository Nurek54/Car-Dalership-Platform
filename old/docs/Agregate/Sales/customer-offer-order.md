classDiagram
direction TB
class Customer {
<<AggregateRoot>>
-CustomerId id
-String fullName
-String nip
-Address address
-ContactData contact
+updateContactDetails(...) void
+verifyTaxId() void
}

    class Offer {
        -OfferId id
        -CustomerId customerId
        -SpecificationId specificationId
        -Money basePrice
        -Money finalPrice
        -OfferState state
        +applyDiscount(Discount discount) void
        +publishOffer() void
        +accept() void
        +reject() void
    }

    class CustomerId{
        <<ValueObject>>
    }

    class OfferState {
        DRAFT
        PUBLISHED
        ACCEPTED
        REJECTED
    }

    class Order {
        -OrderId id
        -OfferId sourceOfferId
        -PaymentMethod paymentMethod
        -Date handoverDate
        -OrderState state
        +declarePaymentMethod(PaymentMethod method) void
        +markAsReadyForHandover() void
        +scheduleHandover(Date date) void
        +confirmHandover() void
        +revertToReadyForHandover() void
    }

    class PaymentMethod {
        BANK_TRANSFER
        FINANCING
    }

    class OrderState {
        IN_PROGRESS
        READY_FOR_HANDOVER
        HANDOVER_SCHEDULED
        COMPLETED
    }

    class OfferId {
    }

    class SpecificationId {
    }

    <<AggregateRoot>> Offer
    <<Enumeration>> OfferState
    <<AggregateRoot>> Order
    <<Enumeration>> PaymentMethod
    <<Enumeration>> OrderState
    <<ValueObject>> OfferId
    <<ValueObject>> SpecificationId

    Offer *-- "1" OfferState
    Offer *-- "1" SpecificationId
    Order *-- "1" OrderState
    Order *-- "1" PaymentMethod
    Order *-- "1" OfferId
    Customer *-- "1" CustomerId
    Offer *-- "1" CustomerId
