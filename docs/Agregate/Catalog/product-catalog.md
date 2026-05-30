classDiagram
direction TB

    %% ==========================================
    %% AGREGAT: PRODUCT CATALOG (Matryca)
    %% ==========================================
    class ProductCatalog {
        <<AggregateRoot>>
        -CatalogId id
        -ModelYear modelYear
        -int version
        -CatalogState state
        +archive() void
    }
    class CatalogId { <<ValueObject>> }
    class ModelYear { <<ValueObject>> }
    class CatalogState {
        <<Enumeration>>
        ACTIVE
        ARCHIVED
    }
    class CatalogOption {
        <<Entity>>
        -OptionCode code
        -Money basePrice
    }
    class CatalogRule {
        <<Entity>>
        -OptionCode sourceCode
        -OptionCode targetCode
        -RuleType type
    }
    class RuleType {
        <<Enumeration>>
        REQUIRES
        EXCLUDES
    }

    ProductCatalog *-- "1" CatalogId
    ProductCatalog *-- "1" ModelYear
    ProductCatalog *-- "1" CatalogState
    ProductCatalog *-- "1..*" CatalogOption : kompozycja lokalna
    ProductCatalog *-- "0..*" CatalogRule : kompozycja lokalna
    CatalogRule *-- "1" RuleType
