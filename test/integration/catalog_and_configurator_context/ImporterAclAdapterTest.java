package integration.catalog_and_configurator_context;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import salon.catalog.application.domain.exception.CatalogValidationException;
import salon.catalog.application.domain.model.catalog.ModelYear;
import salon.catalog.application.domain.model.catalog.RuleType;
import salon.catalog.application.domain.model.shared.OptionCode;
import salon.catalog.application.dto.ImportedCatalogData;
import salon.catalog.infrastructure.out.acl.ExternalCatalogPackage;
import salon.catalog.infrastructure.out.acl.ImporterAclAdapter;
import salon.catalog.infrastructure.out.acl.ImporterCatalogTranslator;
import salon.catalog.infrastructure.out.acl.ImporterServiceClient;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

/** Integracja warstwy ACL — translacja obcego pakietu producenta na lokalny model. */
@SpringBootTest(classes = {ImporterAclAdapter.class, ImporterCatalogTranslator.class})
class ImporterAclAdapterTest {

    @Autowired private ImporterAclAdapter adapter;
    @MockBean private ImporterServiceClient client;

    @Test
    void shouldTranslateExternalPackageIntoLocalModel() {
        // Zewnętrzny pakiet w formacie dostawcy (cena w groszach, ograniczenie INCOMPATIBLE_WITH)
        when(client.downloadLatestPackage()).thenReturn(new ExternalCatalogPackage(
                2025,
                List.of(new ExternalCatalogPackage.ExternalItem("B2", 1_000_000, "PLN")),
                List.of(new ExternalCatalogPackage.ExternalRestriction("B2", "C1", "INCOMPATIBLE_WITH"))));

        ImportedCatalogData data = adapter.fetchLatestCatalog();

        // Obcy JSON zamienia się w hermetyzowane obiekty wartości lokalnego modelu
        assertThat(data.modelYear()).isEqualTo(ModelYear.of(2025));
        assertThat(data.options()).hasSize(1);
        assertThat(data.options().get(0).code()).isEqualTo(OptionCode.of("B2"));
        // 1 000 000 groszy -> 10000.00 jednostek głównych
        assertThat(data.options().get(0).basePrice().amount()).isEqualByComparingTo(new BigDecimal("10000.00"));
        assertThat(data.rules().get(0).type()).isEqualTo(RuleType.EXCLUDES);
    }

    @Test
    void shouldRejectUnknownRestrictionKind() {
        // Nieznany typ ograniczenia dostawcy = błąd translacji formatu (UC-KON-02 / A1)
        when(client.downloadLatestPackage()).thenReturn(new ExternalCatalogPackage(
                2025,
                List.of(new ExternalCatalogPackage.ExternalItem("B2", 1_000_000, "PLN")),
                List.of(new ExternalCatalogPackage.ExternalRestriction("B2", "C1", "FOO_BAR"))));

        // Zamiast brzydkiego błędu rzucany jest kontrolowany wyjątek domenowy
        assertThatThrownBy(() -> adapter.fetchLatestCatalog())
                .isInstanceOf(CatalogValidationException.class);
    }
}
