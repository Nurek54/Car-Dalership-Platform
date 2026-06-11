package salon.sales.domain.model.offer;

/**
 * Cykl życia oferty (proformy) — zgodnie z modelem agregatu
 * (docs/Agregate/Sales/customer-offer-order.md) i PDF (rozdz. 3.3.4):
 * oferta kończy życie w jednoznacznym stanie terminalnym ACCEPTED lub REJECTED,
 * po którym staje się niemutowalnym, historycznym dowodem wynegocjowanych warunków.
 */
public enum OfferState {
    DRAFT,      // "W przygotowaniu" — robocza, można wyceniać i rabatować
    PUBLISHED,  // "Utworzona" — wygenerowana i zaprezentowana klientowi
    ACCEPTED,   // "Zaakceptowana" — klient przyjął warunki (stan terminalny)
    REJECTED    // "Odrzucona" — klient zrezygnował (stan terminalny)
}
