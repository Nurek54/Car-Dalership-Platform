classDiagram
direction TB
class ProductCatalog {
-CatalogId id
-ModelYear modelYear
-int version
-CatalogState state
+archive() void
}

    class CatalogState {
        ACTIVE
        ARCHIVED
    }

    class CatalogOption {
        -OptionCode code
        -Money basePrice
    }

    class RuleType {
        REQUIRES
        EXCLUDES
    }

    class CatalogRule {
        -OptionCode sourceCode
        -OptionCode targetCode
        -RuleType type
    }

    <<AggregateRoot>> ProductCatalog
    <<Enumeration>> CatalogState
    <<ValueObject>> CatalogOption
    <<Enumeration>> RuleType
    <<ValueObject>> CatalogRule

    ProductCatalog *-- "1" CatalogState
    ProductCatalog *-- "1..*" CatalogOption
    ProductCatalog *-- "0..*" CatalogRule
    CatalogRule *-- "1" RuleType
