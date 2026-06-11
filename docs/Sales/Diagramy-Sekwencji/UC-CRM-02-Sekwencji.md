sequenceDiagram
autonumber
participant Bus as Domain Event Bus<br/>(SpecificationCompleted z Katalogu)
participant Sub as CatalogEventSubscriberAdapter
actor H as Handlowiec
participant Sales as SalesAppService<br/>«GenerateOfferUseCase»
participant Cat as CatalogRepository<br/>«out port, ACL Katalogu»
participant OF as OfferFactory
participant Offer as Offer<br/>«aggregate root»
participant Repo as OfferRepository

    note over Bus,Repo: UC-CRM-02 — Wygenerowanie oferty proforma
    Bus-->>Sub: SpecificationCompletedEvent {specificationId, catalogId}
    Sub-->>H: powiadomienie o nowej specyfikacji oczekującej na ofertowanie (krok 1)

    H->>Sales: generateOffer(customerId, specificationId)
    Sales->>Cat: getSpecificationPrice(specificationId)
    note over Cat: wycena katalogowa z modułu Katalogu<br/>(404 -> SpecificationNotFound, awaria -> ExternalServiceUnavailable)
    Cat-->>Sales: Money(basePrice)

    Sales->>OF: createOffer(CustomerId, SpecificationId, basePrice)
    note over OF: walidacja danych oferty — cena musi być<br/>ściśle dodatnia (InvalidOfferDataException)
    OF->>Offer: new Offer(OfferId.generate(), customerId, specificationId, basePrice)
    Offer-->>OF: Offer (state = DRAFT "W przygotowaniu", validityDate = now + 14d)
    OF-->>Sales: Offer
    Sales->>Offer: publishOffer()
    note over Offer: DRAFT -> PUBLISHED ("Utworzona") — dokument proforma<br/>gotowy do prezentacji klientowi (krok 4);<br/>polityka rabatowa (applyDiscount) zamknięta w agregacie
    Sales->>Repo: save(Offer)
    Sales-->>H: OfferId
