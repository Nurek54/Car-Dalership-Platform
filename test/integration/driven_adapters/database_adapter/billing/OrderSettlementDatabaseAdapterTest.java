package integration.driven_adapters.database_adapter.billing;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import salon.billing.domain.model.OrderSettlement;
import salon.billing.domain.model.SettlementId;
import salon.billing.domain.model.SettlementState;
import salon.shared.model.OrderId;
import salon.shared.model.Money;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(OrderSettlementDatabaseAdapter.class)
class OrderSettlementDatabaseAdapterTest {

    @Autowired
    private OrderSettlementDatabaseAdapter adapter;

    // 1. ZAPIS, ODCZYT I MAPOWANIE: Weryfikacja wielu obiektów Value Object (Money)
    @Test
    void shouldSaveAndRetrieveOrderSettlementWithMultipleMoneyFieldsMapped() {
        // Arrange - Tworzymy obiekt rozliczenia z kilkoma źródłami finansowania
        SettlementId settlementId = new SettlementId("SET-2026-001");
        OrderId orderId = new OrderId("ORD-999");

        // Zgodnie z analizą: Wartość pojazdu = 150 000 PLN
        Money vehicleValue = Money.of(new BigDecimal("150000.00"), "PLN");
        // Suma zadatków = 10 000 PLN
        Money totalDeposits = Money.of(new BigDecimal("10000.00"), "PLN");
        // Wartość z leasingu (Kontekst Finansowania) = 100 000 PLN
        Money financingAmount = Money.of(new BigDecimal("100000.00"), "PLN");
        // Wartość starego auta w rozliczeniu (Kontekst Sprzedaży / Trade-in) = 40 000 PLN
        Money tradeInValue = Money.of(new BigDecimal("40000.00"), "PLN");

        OrderSettlement settlement = new OrderSettlement(
                settlementId, orderId, vehicleValue, totalDeposits, financingAmount, tradeInValue
        );

        // Wywołujemy regułę biznesową: Wartość pojazdu - Zadatki - Finansowanie - Odkup
        // W tym przypadku: 150 000 - 10 000 - 100 000 - 40 000 = 0 PLN
        settlement.calculateBalance();
        settlement.checkForOverpayment();

        // Ponieważ saldo wynosi równo 0 (brak niedopłaty i brak nadpłaty),
        // status powinien móc przejść w SETTLED (Rozliczone)
        settlement.changeSettledState();

        // Act - Adapter mapuje te wszystkie obiekty Money na dedykowane kolumny w jednej tabeli i zapisuje
        adapter.save(settlement);

        // Odczyt wymuszający transformację z encji JPA do czystej domeny
        Optional<OrderSettlement> retrievedSettlement = adapter.findById(settlementId);

        // Assert
        assertThat(retrievedSettlement).isPresent();
        OrderSettlement retrieved = retrievedSettlement.get();

        assertThat(retrieved.getId()).isEqualTo(settlementId);
        assertThat(retrieved.getOrderId()).isEqualTo(orderId);
        assertThat(retrieved.getState()).isEqualTo(SettlementState.SETTLED);

        // KLUCZOWE: Weryfikujemy, czy poszczególne kwoty nie "nadpisały się" nawzajem w bazie danych!
        assertThat(retrieved.getVehicleValue().getAmount()).isEqualByComparingTo("150000.00");
        assertThat(retrieved.getTotalDeposits().getAmount()).isEqualByComparingTo("10000.00");
        assertThat(retrieved.getFinancingAmount().getAmount()).isEqualByComparingTo("100000.00");
        assertThat(retrieved.getTradeInValue().getAmount()).isEqualByComparingTo("40000.00");

        // Weryfikujemy finalne saldo wyliczone przez domenę (0 PLN)
        assertThat(retrieved.getFinalBalance().getAmount()).isEqualByComparingTo("0.00");
    }

    // 2. ZAPIS I MAPOWANIE: Nadpłata wymagająca korekty (zgodnie z UC-ROZ-03)
    @Test
    void shouldMapStateCorrectlyWhenOverpaymentOccurs() {
        // Arrange
        SettlementId settlementId = new SettlementId("SET-2026-002");
        OrderId orderId = new OrderId("ORD-888");

        Money vehicleValue = Money.of(new BigDecimal("100000.00"), "PLN");
        // Klient omyłkowo wpłacił 110 000 PLN za auto warte 100 000 PLN
        Money totalDeposits = Money.of(new BigDecimal("110000.00"), "PLN");
        Money zero = Money.of(BigDecimal.ZERO, "PLN");

        OrderSettlement settlement = new OrderSettlement(
                settlementId, orderId, vehicleValue, totalDeposits, zero, zero
        );

        // Act
        settlement.calculateBalance(); // Saldo wyjdzie ujemne (-10 000 PLN)
        settlement.checkForOverpayment(); // Powinno zablokować status i ustawić REQUIRES_CORRECTION
        settlement.changeSettledState();

        adapter.save(settlement);
        Optional<OrderSettlement> retrieved = adapter.findById(settlementId);

        // Assert
        assertThat(retrieved).isPresent();
        // Zgodnie z wymaganiami z dokumentacji, nadpłata wymusza na systemie nałożenie tego statusu
        assertThat(retrieved.get().getState()).isEqualTo(SettlementState.REQUIRES_CORRECTION);
        assertThat(retrieved.get().getFinalBalance().getAmount()).isEqualByComparingTo("-10000.00");
    }

    // 3. BRAK DANYCH
    @Test
    void shouldReturnEmptyOptionalWhenOrderSettlementDoesNotExist() {
        // Act
        Optional<OrderSettlement> result = adapter.findById(new SettlementId("SET-UNKNOWN"));

        // Assert
        assertThat(result).isEmpty();
    }
}