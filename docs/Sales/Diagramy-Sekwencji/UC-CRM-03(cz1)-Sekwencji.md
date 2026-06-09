sequenceDiagram
autonumber
actor H as Handlowiec
participant OrderSvc as OrderAppService
participant OfferRepo as OfferRepository
participant Offer as Offer
participant ORF as OrderFactory
participant Order as Order
participant OrderRepo as OrderRepository

    H->>OrderSvc: createOrderFromOffer(CreateOrderCommand)
    OrderSvc->>OfferRepo: findById(OfferId)
    OfferRepo-->>OrderSvc: Optional<Offer>
    alt offer empty
        OrderSvc-->>H: IllegalStateException("Offer not found")
    else state == EXPIRED or validityDate < today
        OrderSvc-->>H: OfferExpiredException
    else state != PUBLISHED
        OrderSvc-->>H: IllegalStateException("...PUBLISHED offer")
    else
        OrderSvc->>Offer: toSnapshot()
        Offer-->>OrderSvc: OfferSnapshot(OfferId, CustomerId, SpecificationId, finalPrice)
        OrderSvc->>ORF: createFromOffer(OfferId, OfferSnapshot)
        ORF->>Order: new Order(OrderId.generate(), sourceOfferId, requiredDeposit)
        Order-->>ORF: Order (state = DRAFT_CREATED)
        ORF-->>OrderSvc: Order
        OrderSvc->>Order: confirmSignature(signatureRef)
        Order->>Order: state = PENDING_PAYMENT&#59; registerEvent(OrderPlacedEvent)
        OrderSvc->>OrderRepo: save(Order)
        OrderSvc->>Offer: markAsConverted()
        Offer->>Offer: state = CONVERTED
        OrderSvc->>OfferRepo: save(Offer)
        OrderSvc-->>H: orderId (String)
    end
    Note over OrderSvc,Order: createOrderFromOffer rejestruje OrderPlacedEvent w agregacie, ale go NIE publikuje.