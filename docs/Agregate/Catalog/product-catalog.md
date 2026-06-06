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
        <<ValueObject>>
        -OptionCode code
        -Money basePrice
    }
    class CatalogRule {
        <<ValueObject>>
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
    ProductCatalog *-- "1..*" CatalogOption : kompozycja lokalna (obiekty wartości)
    ProductCatalog *-- "0..*" CatalogRule : kompozycja lokalna (obiekty wartości)
    CatalogRule *-- "1" RuleType

    %% NOTE: CatalogOption and CatalogRule are modelled as Value Objects.
    %% ProductCatalog is immutable once activated; options/rules do not
    %% possess an independent lifecycle and therefore should not expose
    %% artificial persistence identifiers in the domain model.
