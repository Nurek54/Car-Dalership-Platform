package salon.billing.application.service;

import salon.billing.application.port.in.CalculateSettlementCommand;
import salon.billing.application.port.in.CalculateSettlementUseCase;
import salon.billing.application.port.out.ExternalIntegrationPort;
import salon.billing.application.port.out.SettlementRepository;
import salon.billing.domain.model.settlement.OrderSettlement;
import salon.billing.domain.model.settlement.SettlementId;
import salon.billing.domain.service.SettlementCalculationService;
import salon.shared.model.Money;
import salon.shared.model.OrderId;

import java.util.Optional;

/**
 * Realizuje UC-ROZ-03 (warstwa aplikacji — orkiestracja). Jedna waluta dla całego rozliczenia.
 */
public class SettlementAppService implements CalculateSettlementUseCase {

    private final SettlementRepository settlementRepository;
    private final ExternalIntegrationPort externalIntegration;
    private final SettlementCalculationService calculationService;

    public SettlementAppService(SettlementRepository settlementRepository,
                                ExternalIntegrationPort externalIntegration,
                                SettlementCalculationService calculationService) {
        if (settlementRepository == null) {
            throw new IllegalArgumentException("settlementRepository must not be null.");
        }
        if (externalIntegration == null) {
            throw new IllegalArgumentException("externalIntegration must not be null.");
        }
        if (calculationService == null) {
            throw new IllegalArgumentException("calculationService must not be null.");
        }
        this.settlementRepository = settlementRepository;
        this.externalIntegration = externalIntegration;
        this.calculationService = calculationService;
    }

    @Override
    // @Transactional w projekcie ze Springiem.
    public void calculateSettlement(CalculateSettlementCommand command) {
        if (command == null) {
            throw new IllegalArgumentException("command must not be null.");
        }

        // 1-3. Dane z zamówienia -> obiekty domenowe.
        OrderId orderId = new OrderId(command.orderId());
        Money vehicleValue = new Money(command.vehicleValue(), command.currency());
        Money totalDeposits = new Money(command.totalDeposits(), command.currency());

        // 4. Finansowanie z Modułu Finansowania (ACL).
        Optional<Money> financing = externalIntegration.getApprovedFinancing(orderId);
        if (financing.isEmpty()) {
            // A2: brak potwierdzenia finansowania -> blokujemy rozliczenie i wydanie pojazdu.
            throw new IllegalStateException(
                    "No approved financing — settlement and vehicle handover are blocked.");
        }
        Money financingAmount = financing.get();

        // 5. Wartość odkupu pojazdu używanego.
        Money tradeInValue = externalIntegration.getTradeInValue(orderId);

        // 6. Budujemy agregat krok po kroku i liczymy saldo (przez serwis dziedzinowy).
        OrderSettlement settlement = new OrderSettlement(SettlementId.generate(), orderId, vehicleValue);
        settlement.applyDeposit(totalDeposits);
        settlement.applyFinancing(financingAmount);
        settlement.applyTradeIn(tradeInValue);
        calculationService.process(settlement); // calculateBalance + checkForOverpayment

        // 7. Rozgałęzienie według wyniku.
        if (settlement.requiresCorrection()) {
            // A1: nadpłata -> punkt rozszerzenia (dyspozycja zwrotu nadpłaty).
        } else {
            settlement.markAsSettled();
        }

        settlementRepository.save(settlement);
    }
}
