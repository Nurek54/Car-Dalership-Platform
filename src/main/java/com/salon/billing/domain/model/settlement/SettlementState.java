package main.java.com.salon.billing.domain.model.settlement;

public enum SettlementState {
    OPEN,                 // rozliczenie otwarte
    REQUIRES_CORRECTION,  // "Wymaga korekty księgowej" — nadpłata (UC-ROZ-03, A1)
    SETTLED               // rozliczone
}
