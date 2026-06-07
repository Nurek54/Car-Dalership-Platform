flowchart TB

classDef port fill:#FCF3CF,stroke:#7D6608,color:#000,stroke-width:2px
classDef app fill:#D5F5E3,stroke:#145A32,color:#000,stroke-width:2px
classDef domain fill:#FADBD8,stroke:#922B21,color:#000,stroke-width:2px
classDef repository fill:#EBDEF0,stroke:#5B2C6F,color:#000,stroke-width:2px
classDef factory fill:#D6EAF8,stroke:#1B4F72,color:#000,stroke-width:2px

%% =====================================================
%% ORDER & SIGNATURE FLOW
%% =====================================================
subgraph ORDER_FLOW["📝 Order & Contract Management"]
direction TB

    CPort[ManageOrderUseCase]
    CApp[OrderAppService]

    Order[(Order)]

    CRepo[[OrderRepository]]
    CSignPort[[ESignaturePort]]
    CEventPort[[EventPublisher]]

    CPort --> CApp
    CApp --> Order
    CApp --> CRepo
    CApp --> CSignPort
    CApp --> CEventPort

end

%% =====================================================
%% ORDER FULFILLMENT FLOW (Fast/Long Track)
%% =====================================================
subgraph FULFILLMENT["🚚 Order Fulfillment"]
direction TB

    FPort[ProcessFulfillmentUseCase]
    FApp[OrderFulfillmentAppService]

    FDomainSvc[VehicleMatchingDomainService]
    OrderFul[(Order)]

    FRepo[[OrderRepository]]
    FFactoryPort[[FactoryIntegrationAclPort]]
    FInvPort[[InventoryQueryPort]]

    FPort --> FApp

    FApp --> FDomainSvc
    FApp --> OrderFul

    FDomainSvc -. queries available VINs .-> FInvPort
    FDomainSvc -. matches & allocates .-> OrderFul

    FApp --> FRepo
    FApp --> FFactoryPort

end

%% =====================================================
%% TRADE-IN & DEMO FLOW
%% =====================================================
subgraph TRADE_IN_DEMO["🚗 Trade-in & Test Drives"]
direction TB

    TPort[ManageAncillaryServicesUseCase]
    TApp[AncillarySalesAppService]

    TradeIn[(TradeInAppraisal)]
    TestDrive[(TestDriveAgreement)]

    TRepo[[TradeInRepository]]
    DRepo[[TestDriveRepository]]

    TPort --> TApp

    TApp --> TradeIn
    TApp --> TestDrive

    TApp --> TRepo
    TApp --> DRepo

end

%% =====================================================
%% OFFER PROCESS FLOW
%% =====================================================
subgraph OFFER_FLOW["📄 Offer & Discount Processing"]
direction TB

    OPort[CreateOfferUseCase]
    OApp[OfferAppService]

    Offer[(Offer)]

    ORepo[[OfferRepository]]
    ODocPort[[DocumentGeneratorPort]]

    OPort --> OApp
    OApp --> Offer
    %% Conversion from Offer to Order is delegated to a factory
    OrderFactory[(OrderFactory)]
    OApp --> OrderFactory
    OrderFactory --> Order
    OApp --> ORepo
    OApp --> ODocPort

end

%% =====================================================
%% DOMAIN EVENTS
%% =====================================================
Event{{DomainEvent}}
Order -. generates .-> Event
OrderFul -. generates .-> Event

class OPort,CPort,FPort,TPort port
class OApp,CApp,FApp,TApp app
class FDomainSvc,Offer,Order,OrderFul,TradeIn,TestDrive,Event domain
class ORepo,ODocPort,CRepo,CSignPort,CEventPort,FRepo,FFactoryPort,FInvPort,TRepo,DRepo repository
class OrderFactory factory
