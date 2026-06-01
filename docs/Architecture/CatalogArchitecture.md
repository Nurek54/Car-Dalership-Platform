flowchart TB

classDef port fill:#FCF3CF,stroke:#7D6608,color:#000,stroke-width:2px
classDef app fill:#D5F5E3,stroke:#145A32,color:#000,stroke-width:2px
classDef domain fill:#FADBD8,stroke:#922B21,color:#000,stroke-width:2px
classDef repository fill:#EBDEF0,stroke:#5B2C6F,color:#000,stroke-width:2px

%% =====================================================
%% SPECIFICATION BUILDER FLOW
%% =====================================================
subgraph SPECIFICATION["⚙️ Specification Building"]
direction TB

    SPort[BuildSpecificationUseCase]
    SApp[SpecificationAppService]
    SDomainSvc[RuleValidationDomainService]

    Specification[(VehicleSpecification)]

    SRepo[[SpecificationRepository]]
    CReadRepo[[CatalogRepository]]
    SEvent[[EventPublisher]]

    SPort --> SApp

    SApp --> SDomainSvc
    SApp --> Specification
    SDomainSvc -. reads rules .-> CReadRepo

    SApp --> SRepo
    SApp --> SEvent

end

%% =====================================================
%% CATALOG VERSIONING FLOW
%% =====================================================
subgraph CATALOG["📚 Catalog Versioning"]
direction TB

    CPort[UpdateCatalogUseCase]
    CApp[CatalogAppService]

    Catalog[(ProductCatalog)]

    CRepo[[CatalogRepository]]
    CImporter[[ImporterApiPort]]

    CPort --> CApp

    CApp --> CImporter
    CApp --> Catalog
    CApp --> CRepo

end

%% =====================================================
%% DOMAIN EVENTS
%% =====================================================
Event{{DomainEvent}}
Specification -. generates .-> Event
Catalog -. generates .-> Event

class SPort,CPort port
class SApp,CApp app
class SDomainSvc,Specification,Catalog,Event domain
class SRepo,CReadRepo,CRepo,SEvent,CImporter repository
