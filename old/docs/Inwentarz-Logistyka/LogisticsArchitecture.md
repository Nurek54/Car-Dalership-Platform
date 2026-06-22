flowchart TB

%% =====================================================
%% STYLE DEFINITIONS
%% =====================================================
classDef port fill:#FCF3CF,stroke:#7D6608,color:#000,stroke-width:2px
classDef app fill:#D5F5E3,stroke:#145A32,color:#000,stroke-width:2px
classDef domain fill:#FADBD8,stroke:#922B21,color:#000,stroke-width:2px
classDef repository fill:#EBDEF0,stroke:#5B2C6F,color:#000,stroke-width:2px
classDef factory fill:#D6EAF8,stroke:#1B4F72,color:#000,stroke-width:2px

%% =====================================================
%% INVENTORY AND LOGISTICS MANAGEMENT
%% =====================================================
subgraph INVENTORY ["Inventory & Logistics Management"]
direction TB

    %% PORTY WEJŚCIOWE
    Port1["ReserveVehicleUseCase"]
    Port2["OrderFactoryVehicleUseCase"]
    Port3["ReceiveVehicleUseCase"]
    Port4["ReleaseReservationUseCase"]
    Port5["PrepareForHandoverUseCase"]
    Port6["ReleaseInventoryUseCase"]

    %% ORKIESTRATOR
    AppSvc["InventoryManagementAppService"]

    %% AGREGAT I FABRYKA
    Factory[("InventoryVehicleFactory")]
    Vehicle[("InventoryVehicle")]

    %% REPOZYTORIA I PORTY WYJŚCIOWE
    Repo[["InventoryRepository"]]
    AclFactory[["FactoryIntegrationAclPort"]]

    %% DODANY PORT DO SPECYFIKACJI
    SpecPort[["SpecificationIntegrationPort"]]

    %% PRZEPŁYW STEROWANIA
    Port1 --> AppSvc
    Port2 --> AppSvc
    Port3 --> AppSvc
    Port4 --> AppSvc
    Port5 --> AppSvc
    Port6 --> AppSvc

    AppSvc --> Factory
    Factory -. creates .-> Vehicle
    AppSvc --> Vehicle

    AppSvc --> Repo
    AppSvc --> AclFactory
    AppSvc --> SpecPort

end

%% =====================================================
%% DOMAIN EVENTS
%% =====================================================
Event{{"DomainEvent Bus"}}

Vehicle -. generates .-> Event

%% =====================================================
%% NODE COLORS
%% =====================================================
class Port1,Port2,Port3,Port4,Port5,Port6 port
class AppSvc app
class Vehicle,Event domain
class Repo,AclFactory,SpecPort repository
class Factory factory
