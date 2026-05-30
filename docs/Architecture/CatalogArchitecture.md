flowchart TB

%% =====================================================
%% STYLE DEFINITIONS
%% =====================================================
classDef inbound fill:#D6EAF8,stroke:#1B4F72,color:#000,stroke-width:2px
classDef port fill:#FCF3CF,stroke:#7D6608,color:#000,stroke-width:2px
classDef app fill:#D5F5E3,stroke:#145A32,color:#000,stroke-width:2px
classDef domain fill:#FADBD8,stroke:#922B21,color:#000,stroke-width:2px
classDef repository fill:#EBDEF0,stroke:#5B2C6F,color:#000,stroke-width:2px
classDef adapter fill:#EAECEE,stroke:#424949,color:#000,stroke-width:2px

%% =====================================================
%% SPECIFICATION BUILDER FLOW (UC-KAT-01)
%% =====================================================
subgraph SPECIFICATION["⚙️ Specification Building"]
direction TB

    SRest[ConfiguratorRestAdapter]
    SPort[BuildSpecificationUseCase]
    SApp[SpecificationAppService]
    SDomainSvc[RuleValidationDomainService]

    Specification[(VehicleSpecification)]

    SRepo[[SpecificationRepository]]
    CReadRepo[[CatalogRepository]]

    SDb[(DatabaseAdapter)]

    SRest --> SPort
    SPort --> SApp

    SApp --> SDomainSvc
    SApp --> Specification
    SDomainSvc -. reads rules .-> CReadRepo

    SApp --> SRepo

    SDb -. implements .-> SRepo
    SDb -. implements .-> CReadRepo

end

%% =====================================================
%% CATALOG VERSIONING FLOW (UC-KAT-02)
%% =====================================================
subgraph CATALOG["📚 Catalog Versioning"]
direction TB

    CCron[CatalogUpdateCronJob]
    CPort[UpdateCatalogUseCase]

    CApp[CatalogAppService]

    Catalog[(ProductCatalog)]

    CRepo[[CatalogRepository]]
    CImporter[[ImporterApiPort]]

    CDb[(DatabaseAdapter)]
    CApi[(ImporterHttpAdapter)]

    CCron --> CPort
    CPort --> CApp

    CApp --> CImporter
    CApp --> Catalog
    CApp --> CRepo

    CDb -. implements .-> CRepo
    CApi -. implements .-> CImporter

end

%% =====================================================
%% DOMAIN EVENTS
%% =====================================================
Event{{DomainEvent}}

Specification -. generates .-> Event
Catalog -. generates .-> Event

%% =====================================================
%% NODE COLORS
%% =====================================================
class SRest,CCron inbound
class SPort,CPort port
class SApp,CApp app
class SDomainSvc,Specification,Catalog,Event domain
class SRepo,CReadRepo,CRepo,CImporter repository
class SDb,CDb,CApi adapter
