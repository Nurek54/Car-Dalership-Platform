classDiagram
direction TB

    class Vehicle {
        <<AggregateRoot>>
        -VinNumber vin
        -VehicleRole role
        -VehicleState state
        -PdiStatus pdiStatus
        -LocalDate pdiApprovalDate
        -LocalDate yardEntryDate
        -OrderId lockedForOrder
        +receiveOnYard(ImporterData data) void
        +approvePdi() void
        +expirePdiValidity() void
        +markAsDemo(int currentMileage) void
        +lockForOrder(OrderId orderId) void
        +releaseReservation() void
    }
    class VinNumber { <<ValueObject>> }
    class OrderId { <<ValueObject>> }
    class VehicleRole {
        <<Enumeration>>
        STOCK
        DEMO
    }
    class VehicleState {
        <<Enumeration>>
        IN_TRANSIT
        ON_YARD
        RESERVED
        HANDED_OVER
        TRANSPORT_DAMAGE
    }
    class PdiStatus {
        <<Enumeration>>
        PENDING
        APPROVED
        EXPIRED
        PRE_SALE_DEFECT
    }

    Vehicle *-- "1" VinNumber
    Vehicle *-- "0..1" OrderId : referencja rozłączna
    Vehicle *-- "1" VehicleRole
    Vehicle *-- "1" VehicleState
    Vehicle *-- "1" PdiStatus
