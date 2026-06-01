package salon.billing.application.port.in;

// Port wejściowy dla UC-ROZ-03.
public interface CalculateSettlementUseCase {
    void calculateSettlement(CalculateSettlementCommand command);
}
