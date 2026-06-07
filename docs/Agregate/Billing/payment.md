classDiagram
direction TB
class Payment {
<<AggregateRoot>>
-PaymentId id
-OrderId orderId
-Money amount
-PaymentCategory category
+categorizePayment(Money requiredDeposit) void
}
class PaymentId {
<<ValueObject>>
+UUID value
}
class OrderId {
<<ValueObject>>
+String value
}
class Money {
<<ValueObject>>
+BigDecimal amount
+String currency
}
class PaymentCategory {
<<Enumeration>>
DEPOSIT
ADVANCE
FINAL_PAYMENT
}

    Payment *-- "1" PaymentId
    Payment *-- "1" OrderId : referencja
    Payment *-- "1" Money
    Payment *-- "1" PaymentCategory
