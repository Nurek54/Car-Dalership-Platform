flowchart TB

classDef port fill:#FCF3CF,stroke:#7D6608,color:#000,stroke-width:2px
classDef app fill:#D5F5E3,stroke:#145A32,color:#000,stroke-width:2px
classDef domain fill:#FADBD8,stroke:#922B21,color:#000,stroke-width:2px
classDef repository fill:#EBDEF0,stroke:#5B2C6F,color:#000,stroke-width:2px

%% =====================================================
%% PHYSICAL YARD & PDI MANAGEMENT (UC-INW-01, 02, 03, 05)
%% =====================================================
subgraph PHYSICAL_YARD["🚗 Physical Yard & PDI Management"]
direction TB

    YPort[ManageYardUseCase]
    YApp[YardManagementAppService]

    Vehicle[(InventoryVehicle)]

    YRepo[[VehicleRepository]]
    YEvent[[EventPublisher]]
    YImporterPort[[ImporterIdentityAclPort]]

    YPort --> YApp

    YApp --> Vehicle

    YApp --> YRepo
    YApp --> YEvent
    YApp --> YImporterPort

end

%% =====================================================
%% PRODUCTION TRACKING (UC-INW-04)
%% =====================================================
subgraph PRODUCTION_TRACKING["🏭 Production Tracking"]
direction TB

    PPort[TrackProductionUseCase]
    PApp[ProductionTrackingAppService]

    Slot[(ProductionSlot)]

    PRepo[[ProductionSlotRepository]]
    PFactoryPort[[FactoryStatusAclPort]]

    PPort --> PApp

    PApp --> Slot

    PApp --> PRepo
    PApp --> PFactoryPort

end

%% =====================================================
%% ALLOCATION & RESERVATION (UC-INW-06, 07)
%% =====================================================
subgraph RESERVATION["🔒 Reservation & Order Events"]
direction TB

    RPort[HandleOrderEventsUseCase]
    RApp[AllocationAppService]
    RDomainSvc[VehicleAllocationDomainService]

    RPort --> RApp

    RApp --> RDomainSvc

    RDomainSvc -. locks for order .-> Vehicle
    RDomainSvc -. creates for order .-> Slot

end

%% =====================================================
%% DOMAIN EVENTS
%% =====================================================
Event{{DomainEvent}}
Vehicle -. generates .-> Event

class YPort,PPort,RPort port
class YApp,PApp,RApp app
class Vehicle,Slot,RDomainSvc,Event domain
class YRepo,YEvent,YImporterPort,PRepo,PFactoryPort repository
