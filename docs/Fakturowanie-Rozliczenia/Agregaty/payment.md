classDiagram
direction TB
class Payment {
<<AggregateRoot>>
-PaymentId id
-OrderId orderId
-Money amount
-PaymentStatus status
-PaymentCategory category
+createUnassignedPayment(PaymentId id, Money amount) Payment
+createPayment(PaymentId id, OrderId orderId, Money amount) Payment
+assignToOrder(OrderId orderId) void
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
class PaymentStatus {
<<Enumeration>>
UNASSIGNED
ASSIGNED
}

    Payment *-- "1" PaymentId
    Payment *-- "0..1" OrderId : referencja po przypisaniu
    Payment *-- "1" Money
    Payment *-- "1" PaymentStatus
    Payment *-- "1" PaymentCategory
