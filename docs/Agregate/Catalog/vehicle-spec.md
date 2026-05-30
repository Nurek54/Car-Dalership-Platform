classDiagram
direction TB

    %% ==========================================
    %% AGREGAT: VEHICLE SPECIFICATION (Konfiguracja)
    %% ==========================================
    class VehicleSpecification {
        <<AggregateRoot>>
        -SpecificationId id
        -CatalogId catalogId
        -Money totalPrice
        -SpecificationState state
        +addOption(OptionCode code, ProductCatalog catalog) void
        +removeOption(OptionCode code) void
        +finalizeSpecification() void
    }
    class SpecificationId { <<ValueObject>> }
    class CatalogId { <<ValueObject>> }
    class OptionCode { <<ValueObject>> }
    class SpecificationState {
        <<Enumeration>>
        DRAFT
        READY_FOR_SALES
    }
    class Money { <<ValueObject>> }

    VehicleSpecification *-- "1" SpecificationId
    VehicleSpecification *-- "1" CatalogId : referencja do cennika
    VehicleSpecification *-- "1" SpecificationState
    VehicleSpecification *-- "1" Money
    VehicleSpecification *-- "0..*" OptionCode : lista wybranych opcji
