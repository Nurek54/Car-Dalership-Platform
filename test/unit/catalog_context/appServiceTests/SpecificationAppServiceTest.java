package unit.catalog_context.appServiceTests;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.catalog.application.port.out.SpecificationRepository;
import salon.catalog.application.service.SpecificationAppService;
import salon.catalog.domain.event.SpecificationCompletedEvent;
import salon.catalog.domain.model.catalog.CatalogId;
import salon.catalog.domain.model.catalog.CatalogOption;
import salon.catalog.domain.model.catalog.OptionCode;
import salon.catalog.domain.model.catalog.ProductCatalog;
import salon.catalog.domain.model.specification.RuleViolationException;
import salon.catalog.domain.model.specification.VehicleSpecification;
import salon.catalog.domain.service.RuleValidationDomainService;
import salon.shared.application.EventPublisherPort;
import salon.shared.model.Money;
import salon.shared.model.SpecificationId;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/** UC-KON-01: Budowa specyfikacji pojazdu w konfiguratorze */
@ExtendWith(MockitoExtension.class)
class SpecificationAppServiceTest {

    @Mock private SpecificationRepository specificationRepository;
    @Mock private RuleValidationDomainService ruleValidation;
    @Mock private EventPublisherPort eventPublisher;
    @InjectMocks private SpecificationAppService specificationAppService;

    @Test
    void shouldStartNewSpecificationAndSaveIt() {
        // Handlowiec otwiera nową konfigurację dla aktywnego cennika
        SpecificationId id = specificationAppService.startSpecification("CAT-1");

        // Świeża specyfikacja (DRAFT) trafia do repozytorium, a wołający dostaje jej identyfikator
        assertThat(id).isNotNull();
        verify(specificationRepository).save(any(VehicleSpecification.class));
    }

    @Test
    void shouldDelegateOptionValidationToDomainServiceAndSave() {
        // Istniejąca specyfikacja w repozytorium
        VehicleSpecification specification =
                new VehicleSpecification(new SpecificationId("SPEC-1"), new CatalogId("CAT-1"));
        when(specificationRepository.findById(new SpecificationId("SPEC-1")))
                .thenReturn(Optional.of(specification));

        specificationAppService.addOption("SPEC-1", "CAT-1", "LED_LIGHTS");

        // Aplikacja jest cienka: walidację reguł wykonuje serwis dziedzinowy (Fail-fast)
        verify(ruleValidation).validateAndAddOption(eq(specification), eq(new OptionCode("LED_LIGHTS")));
        verify(specificationRepository).save(specification);
    }

    @Test
    void shouldNotSaveWhenRuleValidationFails() {
        // Specyfikacja istnieje, ale dobierana opcja łamie regułę wykluczenia
        VehicleSpecification specification =
                new VehicleSpecification(new SpecificationId("SPEC-2"), new CatalogId("CAT-1"));
        when(specificationRepository.findById(new SpecificationId("SPEC-2")))
                .thenReturn(Optional.of(specification));
        doThrow(new RuleViolationException("Option A is mutually exclusive with B"))
                .when(ruleValidation).validateAndAddOption(any(), any());

        // Wyjątek dziedzinowy wypływa do wołającego
        assertThatThrownBy(() -> specificationAppService.addOption("SPEC-2", "CAT-1", "A"))
                .isInstanceOf(RuleViolationException.class);

        // Niespójna konfiguracja nie zostaje utrwalona
        verify(specificationRepository, never()).save(any());
    }

    @Test
    void shouldFailWhenSpecificationDoesNotExist() {
        // Brak specyfikacji o podanym identyfikatorze
        when(specificationRepository.findById(new SpecificationId("SPEC-404")))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> specificationAppService.addOption("SPEC-404", "CAT-1", "LED_LIGHTS"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Specification not found");

        verifyNoInteractions(ruleValidation);
    }

    @Test
    void shouldFinalizeSpecificationAndPublishCompletedEvent() {
        // Specyfikacja z jedną wybraną opcją (warunek finalizacji)
        ProductCatalog catalog = ProductCatalog.createActive("MY_2026");
        catalog.addOption(new CatalogOption(new OptionCode("LED_LIGHTS"), Money.of(4500, "PLN")));
        VehicleSpecification specification =
                new VehicleSpecification(new SpecificationId("SPEC-3"), catalog.getCatalogId());
        specification.addOption(new OptionCode("LED_LIGHTS"), catalog);
        when(specificationRepository.findById(new SpecificationId("SPEC-3")))
                .thenReturn(Optional.of(specification));

        specificationAppService.finalizeSpecification("SPEC-3");

        // Serwis dziedzinowy ocenia kompletność (REQUIRES), zapis i publikacja zdarzenia
        verify(ruleValidation).assertComplete(specification);
        verify(specificationRepository).save(specification);
        verify(eventPublisher).publish(any(SpecificationCompletedEvent.class));
    }

    @Test
    void shouldBlockFinalizationWhenConfigurationIsIncomplete() {
        // Kompletność konfiguracji nie jest spełniona (reguła REQUIRES)
        VehicleSpecification specification =
                new VehicleSpecification(new SpecificationId("SPEC-4"), new CatalogId("CAT-1"));
        when(specificationRepository.findById(new SpecificationId("SPEC-4")))
                .thenReturn(Optional.of(specification));
        doThrow(new RuleViolationException("Option A requires B to be selected."))
                .when(ruleValidation).assertComplete(specification);

        assertThatThrownBy(() -> specificationAppService.finalizeSpecification("SPEC-4"))
                .isInstanceOf(RuleViolationException.class);

        // Finalizacja przerwana: brak zapisu i brak zdarzenia
        verify(specificationRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }
}
