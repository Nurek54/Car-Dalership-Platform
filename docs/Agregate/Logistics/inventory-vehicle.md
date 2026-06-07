classDiagram
direction TB

    class InventoryVehicle {
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

    InventoryVehicle *-- "1" VinNumber
    InventoryVehicle *-- "0..1" OrderId : referencja rozłączna
    InventoryVehicle *-- "1" VehicleRole
    InventoryVehicle *-- "1" VehicleState
    InventoryVehicle *-- "1" PdiStatus
