classDiagram
direction TB

    class ProductionSlot {
        <<AggregateRoot>>
        -SlotId id
        -OrderId orderId
        -String factoryJobId
        -LocalDate estimatedDelivery
        -SlotState state
        +createForOrder(OrderId orderId, String[] specCodes) ProductionSlot$
        +updateFactoryStatus(FactoryStatus status, LocalDate eta) void
        +cancelSlot() void
    }
    class SlotId { <<ValueObject>> }
    class OrderId { <<ValueObject>> }
    class SlotState {
        <<Enumeration>>
        SCHEDULED
        IN_PRODUCTION
        IN_TRANSIT
        CANCELLED
    }

    ProductionSlot *-- "1" SlotId
    ProductionSlot *-- "1" OrderId : referencja do zamówienia
    ProductionSlot *-- "1" SlotState
