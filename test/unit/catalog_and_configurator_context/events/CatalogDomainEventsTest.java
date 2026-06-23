package unit.catalog_and_configurator_context.events;

import org.junit.jupiter.api.Test;
import salon.catalog.application.domain.model.catalog.CatalogId;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.event.CatalogUpdateFailed;
import salon.catalog.application.domain.model.event.CatalogUpdated;
import salon.catalog.application.domain.model.event.SpecificationCompleted;
import salon.catalog.application.domain.model.shared.Money;
import salon.catalog.application.domain.model.shared.OptionCode;
import salon.catalog.application.domain.model.specification.SpecificationId;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/** Zdarzenia dziedzinowe kontekstu Katalogu i Konfiguratora (Tabela 9). */
class CatalogDomainEventsTest {

    @Test
    void specificationCompletedShouldCarryConfigurationData() {
        SpecificationId specId = SpecificationId.generate();
        CatalogId catalogId = CatalogId.generate();
        Instant now = Instant.now();

        // UC-KON-01, krok 7: zdarzenie emitowane po finalizacji specyfikacji
        SpecificationCompleted event = new SpecificationCompleted(
                specId, catalogId, Money.of(new BigDecimal("12000"), "PLN"),
                List.of(OptionCode.of("B2"), OptionCode.of("A1")), now);

        assertThat(event.eventName()).isEqualTo("SpecificationCompleted");
        assertThat(event.specificationId()).isEqualTo(specId);
        assertThat(event.catalogId()).isEqualTo(catalogId);
        assertThat(event.totalPrice()).isEqualTo(Money.of(new BigDecimal("12000"), "PLN"));
        assertThat(event.optionsPicked()).containsExactly(OptionCode.of("B2"), OptionCode.of("A1"));
        assertThat(event.occurredOn()).isEqualTo(now);
    }

    @Test
    void specificationCompletedShouldExposeImmutableOptionsList() {
        // Zdarzenia są niemutowalne — lista opcji jest kopią tylko do odczytu
        List<OptionCode> source = new ArrayList<>(List.of(OptionCode.of("B2")));
        SpecificationCompleted event = new SpecificationCompleted(
                SpecificationId.generate(), CatalogId.generate(),
                Money.of(BigDecimal.TEN, "PLN"), source, Instant.now());

        // Modyfikacja źródłowej listy nie wpływa na zdarzenie
        source.add(OptionCode.of("C1"));
        assertThat(event.optionsPicked()).containsExactly(OptionCode.of("B2"));
        // a sama lista zdarzenia jest niemodyfikowalna
        assertThatThrownBy(() -> event.optionsPicked().add(OptionCode.of("Z1")))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void catalogUpdatedShouldCarryVersionInfo() {
        CatalogId catalogId = CatalogId.generate();
        Instant now = Instant.now();

        // UC-KON-02, krok 5: zdarzenie po pomyślnej aktualizacji cennika
        CatalogUpdated event = new CatalogUpdated(catalogId, ModelYear.of(2025), 2, now);

        assertThat(event.eventName()).isEqualTo("CatalogUpdated");
        assertThat(event.catalogId()).isEqualTo(catalogId);
        assertThat(event.modelYear()).isEqualTo(ModelYear.of(2025));
        assertThat(event.version()).isEqualTo(2);
        assertThat(event.occurredOn()).isEqualTo(now);
    }

    @Test
    void catalogUpdateFailedShouldCarryReason() {
        Instant now = Instant.now();

        // UC-KON-02 / A1: techniczny błąd integracji
        CatalogUpdateFailed event = new CatalogUpdateFailed("Niezgodny format pakietu", now);

        assertThat(event.eventName()).isEqualTo("CatalogUpdateFailed");
        assertThat(event.reason()).isEqualTo("Niezgodny format pakietu");
        assertThat(event.occurredOn()).isEqualTo(now);
    }
}
