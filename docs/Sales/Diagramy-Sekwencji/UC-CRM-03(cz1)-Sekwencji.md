sequenceDiagram
autonumber
actor K as Klient (front-end)
participant REST as OfferRestApiAdapter
participant Sales as SalesAppService<br/>«AcceptOffer/CreateOrderUseCase»
participant OfferRepo as OfferRepository
participant Offer as Offer<br/>«aggregate root»
participant ORF as OrderFactory
participant Order as Order<br/>«aggregate root»
participant OrderRepo as OrderRepository
participant Pub as EventPublisherPort
participant Inv as InventoryIntegrationPort<br/>«out port»

    note over K,Inv: UC-CRM-03 (cz.1) — Zatwierdzenie oferty i utworzenie zamówienia
    K->>REST: POST /api/sales/offers/{offerId}/accept
    REST->>Sales: acceptOfferAndCreateOrder(OfferId)
    Sales->>OfferRepo: findById(OfferId)
    alt oferta nie istnieje
        OfferRepo-->>Sales: Optional.empty()
        Sales-->>REST: OfferNotFoundException -> 404 Not Found
    else oferta odnaleziona (PUBLISHED)
        OfferRepo-->>Sales: Offer

        Sales->>Offer: accept()
        note over Offer: reguły W AGREGACIE:<br/>tylko PUBLISHED można zaakceptować (InvalidOfferState);<br/>REJECTED jest niemutowalne (OfferImmutable);<br/>validityDate < dziś -> OfferExpiredException<br/>PUBLISHED -> ACCEPTED ("Zaakceptowana")
        Sales->>Offer: toSnapshot()
        note over Offer: migawka WYŁĄCZNIE z oferty ACCEPTED
        Offer-->>Sales: OfferSnapshot(OfferId, CustomerId, SpecificationId, finalPrice)

        Sales->>ORF: createFromOffer(OfferId, OfferSnapshot)
        ORF->>Order: new Order(OrderId.generate(), sourceOfferId, requiredDeposit)
        note over Order: DRAFT_CREATED -> markPlaced() -> DRAFT;<br/>registerEvent(OrderPlacedEvent)
        ORF-->>Sales: Order

        Sales->>OfferRepo: save(Offer)
        Sales->>OrderRepo: save(Order)
        Sales->>Pub: publishAll([OrderPlacedEvent])
        note over Pub: Rozliczenia: inicjalizacja agregatu salda

        Sales->>Inv: allocateVehicleOrProductionSlot(orderId)
        note over Inv: Inwentarz: UC-INW-01 (rezerwacja z placu)<br/>lub UC-INW-02 (slot produkcyjny)
        Sales-->>REST: orderId
        REST-->>K: 200 OK {orderId}
    end

    note over K,Offer: A1 — Klient odrzuca ofertę: POST /offers/{id}/reject -> Offer.reject()<br/>(PUBLISHED -> REJECTED), zamówienie nie powstaje, nic nie jest emitowane.
    note over Order: Deklaracja formy płatności: Order.declarePaymentMethod(BANK_TRANSFER|FINANCING)<br/>-> BankTransferDeclaredEvent (z kwotą) LUB FinancingRequestedEvent (UC-FIN-01).
