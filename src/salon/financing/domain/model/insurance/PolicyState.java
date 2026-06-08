package salon.financing.domain.model.insurance;

public enum PolicyState {
    CALCULATING,         // trwa wyliczanie wartości
    PENDING_ACTIVATION,  // wartość przypisana, czeka na aktywację u ubezpieczyciela
    ACTIVE               // polisa aktywna
}
