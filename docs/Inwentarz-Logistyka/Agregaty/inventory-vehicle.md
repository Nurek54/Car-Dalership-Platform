classDiagram
direction TB

    class InventoryVehicle {
        <<AggregateRoot>>
        -VinNumber vin
        -VehicleRole role
        -VehicleState state
        -OrderId order
        +receiveOnYard(ImporterData data) void
        +markAsDemo() void
        +lockForOrder(OrderId orderId) void
        +releaseReservation() void
    }
    class OrderId { <<ValueObject>> }
    class VehicleRole {
        <<Enumeration>>
        STOCK
        DEMO
    }
    class VehicleState {
        <<Enumeration>>
        ON_STOCK
        IN_PRODUCTION
        RESERVED
        HANDED_OVER
    }

    InventoryVehicle *-- "0..1" OrderId : referencja rozłączna
    InventoryVehicle *-- "1" VehicleRole
    InventoryVehicle *-- "1" VehicleState
