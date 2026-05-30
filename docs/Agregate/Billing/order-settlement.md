classDiagram
direction TB
class OrderSettlement {
<<AggregateRoot>>
-SettlementId id
-OrderId orderId
-Money vehicleValue
-Money totalDeposits
-Money financingAmount
-Money tradeInValue
-Money finalBalance
-SettlementState state
+calculateBalance() void
+checkForOverpayment() void
}
class SettlementId {
<<ValueObject>>
+UUID value
}
class OrderId {
<<ValueObject>>
+String value
}
class SettlementState {
<<Enumeration>>
OPEN
REQUIRES_CORRECTION
SETTLED
}
class Money {
<<ValueObject>>
+BigDecimal amount
+String currency
}

    OrderSettlement *-- "1" SettlementId : kompozycja (tożsamość)
    OrderSettlement *-- "1" OrderId : kompozycja (referencja)
    OrderSettlement *-- "1" SettlementState : kompozycja
    OrderSettlement *-- "5" Money : kompozycja (wartości finansowe)
