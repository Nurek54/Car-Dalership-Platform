classDiagram
direction TB
class VehicleSpecification {
-SpecificationId id
-CatalogId catalogId
-Money totalPrice
-SpecificationState state
-List~OptionCode~ optionsPicked
+addOption(OptionCode code, ProductCatalog catalog) void
+removeOption(OptionCode code) void
+finalizeSpecification() void
}

    class CatalogId {
    }

    class SpecificationState {
        DRAFT
        IN_PROGRESS
        FINAL
    }

    class OptionCode {
    }

    <<AggregateRoot>> VehicleSpecification
    <<ValueObject>> CatalogId
    <<Enumeration>> SpecificationState
    <<ValueObject>> OptionCode

    VehicleSpecification *-- "1" CatalogId : referencja do cennika
    VehicleSpecification *-- "1" SpecificationState
    VehicleSpecification *-- "4" OptionCode : lista wybranych opcji
