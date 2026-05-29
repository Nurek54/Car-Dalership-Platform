package main.java.com.salon.billing.domain.service;

import main.java.com.salon.billing.application.port.in.CalculateSettlementCommand;
import main.java.com.salon.billing.application.port.in.CalculateSettlementUseCase;
import main.java.com.salon.billing.application.port.out.ExternalIntegrationPort;
import main.java.com.salon.billing.application.port.out.SettlementRepository;
import main.java.com.salon.billing.domain.model.settlement.OrderSettlement;
import main.java.com.salon.billing.domain.model.settlement.SettlementId;
import main.java.com.salon.billing.domain.model.shared.Money;
import main.java.com.salon.billing.domain.model.shared.OrderId;
import main.java.com.salon.billing.domain.service.SettlementCalculationService;

import java.util.Optional;

/**
 * Realizuje UC-ROZ-03.
 * Zakładamy jedną walutę (np. PLN) dla całego rozliczenia.
 */
public class SettlementAppService implements CalculateSettlementUseCase {

    private final SettlementRepository settlementRepository;
    private final ExternalIntegrationPort externalIntegration;
    private final SettlementCalculationService calculationService;

    public SettlementAppService(SettlementRepository settlementRepository,
                                ExternalIntegrationPort externalIntegration,
                                SettlementCalculationService calculationService) {
        this.settlementRepository = settlementRepository;
        this.externalIntegration = externalIntegration;
        this.calculationService = calculationService;
    }

    @Override
    // @Transactional w projekcie ze Springiem.
    public void calculateSettlement(CalculateSettlementCommand command) {
        // 1-3. Dane z zamówienia -> obiekty domenowe.
        OrderId orderId = new OrderId(command.orderId());
        Money vehicleValue = new Money(command.vehicleValue(), command.currency());
        Money totalDeposits = new Money(command.totalDeposits(), command.currency());

        // 4. Finansowanie z Modułu Finansowania (ACL).
        Optional<Money> financing = externalIntegration.getApprovedFinancing(orderId);
        if (financing.isEmpty()) {
            // A2: brak potwierdzenia finansowania -> blokujemy rozliczenie i wydanie pojazdu.
            throw new IllegalStateException(
                    "Brak zatwierdzonego finansowania — nie można rozliczyć ani wydać pojazdu.");
        }
        Money financingAmount = financing.get();

        // 5. Wartość odkupu pojazdu używanego.
        Money tradeInValue = externalIntegration.getTradeInValue(orderId);

        // 6. Tworzymy agregat i liczymy saldo (przez serwis dziedzinowy).
        OrderSettlement settlement = new OrderSettlement(
                SettlementId.generate(), orderId, vehicleValue,
                totalDeposits, financingAmount, tradeInValue);
        calculationService.process(settlement); // calculateBalance + checkForOverpayment

        // 7. Rozgałęzienie według wyniku.
        if (settlement.requiresCorrection()) {
            // A1: nadpłata. Tu (w realnym systemie) generujemy dyspozycję zwrotu nadpłaty.
            // -> punkt rozszerzenia
        } else {
            // Tu (w realnym systemie) generujemy dokument rozliczeniowy dla klienta.
            settlement.markAsSettled();
        }

        // Zapis stanu rozliczenia.
        settlementRepository.save(settlement);
    }
}