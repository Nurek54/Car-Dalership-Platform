sequenceDiagram
autonumber
actor H as Handlowiec
participant OfferSvc as OfferAppService
participant OF as OfferFactory
participant Offer as Offer
participant Repo as OfferRepository

    H->>OfferSvc: createOffer(CreateOfferCommand)
    OfferSvc->>OF: createOffer(CustomerId, SpecificationId, basePrice?)
    OF->>Offer: new Offer(OfferId.generate(), customerId, specificationId)
    Offer-->>OF: Offer (state = DRAFT, validityDate = now + 14d)
    opt basePrice != null
        OF->>Offer: setBasePrice(Money)
        Offer->>Offer: recomputeFinalPrice()
    end
    OF-->>OfferSvc: Offer
    opt discountPercentage != null and salespersonDiscountLimit != null
        OfferSvc->>Offer: applyDiscount(Discount, DiscountLimit)
        alt percentage > maxAllowed
            Offer->>Offer: state = PENDING_DIRECTOR_APPROVAL
        else within limit
            Offer->>Offer: recomputeFinalPrice() (state = DRAFT)
        end
    end
    OfferSvc->>Repo: save(Offer)
    OfferSvc-->>H: OfferId