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
%% SALES & CRM CONTEXT
%% =====================================================
subgraph SALES ["Sales & CRM Context"]
direction TB

    %% PORTY WEJŚCIOWE
    UC1["tartConfiguratorUseCase"]
    UC2["IssueProformaUseCase"]
    UC3["AcceptOffer/CreateOrderUseCase"]
    UC4["ScheduleHandoverUseCase"]
    UC5["ReleaseVehicleUseCase"]

    %% ORKIESTRATOR
    AppSvc["SalesAppService"]

    %% AGREGATY I FABRYKI
    Customer[("Customer")]
    Offer[("Offer")]
    OfferFact[("OfferFactory")]
    Order[("Order")]
    OrderFact[("OrderFactory")]

    %% REPOZYTORIA I INTEGRACJE
    Repo[["Repositories (Customer, Offer, Order)"]]
    InvPort[["InventoryIntegrationPort"]]
    FinPort[["FinancingIntegrationPort"]]

    %% PRZEPŁYWY
    UC1 & UC2 & UC3 & UC4 & UC5 --> AppSvc

    AppSvc --> OfferFact
    OfferFact -. creates .-> Offer

    AppSvc --> OrderFact
    OrderFact -. creates .-> Order

    AppSvc --> Customer
    AppSvc --> Repo
    AppSvc --> InvPort
    AppSvc --> FinPort

end

%% =====================================================
%% COLORS
%% =====================================================
class UC1,UC2,UC3,UC4,UC5 port
class AppSvc app
class Customer,Offer,Order domain
class Repo,InvPort,FinPort repository
class OfferFact,OrderFact factory
