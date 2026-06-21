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

/** UC-KON-01: Building the vehicle specification in the configurator */
@ExtendWith(MockitoExtension.class)
class SpecificationAppServiceTest {

    @Mock private SpecificationRepository specificationRepository;
    @Mock private RuleValidationDomainService ruleValidation;
    @Mock private EventPublisherPort eventPublisher;
    @InjectMocks private SpecificationAppService specificationAppService;

    @Test
    void shouldStartNewSpecificationAndSaveIt() {
        // The Salesperson opens a new configuration for the active price list
        SpecificationId id = specificationAppService.startSpecification("CAT-1");

        // A fresh specification (DRAFT) goes to the repository, and the caller gets its identifier
        assertThat(id).isNotNull();
        verify(specificationRepository).save(any(VehicleSpecification.class));
    }

    @Test
    void shouldDelegateOptionValidationToDomainServiceAndSave() {
        // An existing specification in the repository
        VehicleSpecification specification =
                new VehicleSpecification(new SpecificationId("SPEC-1"), new CatalogId("CAT-1"));
        when(specificationRepository.findById(new SpecificationId("SPEC-1")))
                .thenReturn(Optional.of(specification));

        specificationAppService.addOption("SPEC-1", "CAT-1", "LED_LIGHTS");

        // The application is thin: rule validation is performed by the domain service (Fail-fast)
        verify(ruleValidation).validateAndAddOption(eq(specification), eq(new OptionCode("LED_LIGHTS")));
        verify(specificationRepository).save(specification);
    }

    @Test
    void shouldNotSaveWhenRuleValidationFails() {
        // The specification exists, but the option being added breaks an exclusion rule
        VehicleSpecification specification =
                new VehicleSpecification(new SpecificationId("SPEC-2"), new CatalogId("CAT-1"));
        when(specificationRepository.findById(new SpecificationId("SPEC-2")))
                .thenReturn(Optional.of(specification));
        doThrow(new RuleViolationException("Option A is mutually exclusive with B"))
                .when(ruleValidation).validateAndAddOption(any(), any());

        // The domain exception propagates to the caller
        assertThatThrownBy(() -> specificationAppService.addOption("SPEC-2", "CAT-1", "A"))
                .isInstanceOf(RuleViolationException.class);

        // The inconsistent configuration is not persisted
        verify(specificationRepository, never()).save(any());
    }

    @Test
    void shouldFailWhenSpecificationDoesNotExist() {
        // No specification with the given identifier
        when(specificationRepository.findById(new SpecificationId("SPEC-404")))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> specificationAppService.addOption("SPEC-404", "CAT-1", "LED_LIGHTS"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Specification not found");

        verifyNoInteractions(ruleValidation);
    }

    @Test
    void shouldFinalizeSpecificationAndPublishCompletedEvent() {
        // A specification with one selected option (a finalization precondition)
        ProductCatalog catalog = ProductCatalog.createActive("MY_2026");
        catalog.addOption(new CatalogOption(new OptionCode("LED_LIGHTS"), Money.of(4500, "PLN")));
        VehicleSpecification specification =
                new VehicleSpecification(new SpecificationId("SPEC-3"), catalog.getCatalogId());
        specification.addOption(new OptionCode("LED_LIGHTS"), catalog);
        when(specificationRepository.findById(new SpecificationId("SPEC-3")))
                .thenReturn(Optional.of(specification));

        specificationAppService.finalizeSpecification("SPEC-3");

        // The domain service assesses completeness (REQUIRES), then saving and event publication
        verify(ruleValidation).assertComplete(specification);
        verify(specificationRepository).save(specification);
        verify(eventPublisher).publish(any(SpecificationCompletedEvent.class));
    }

    @Test
    void shouldBlockFinalizationWhenConfigurationIsIncomplete() {
        // The configuration completeness is not satisfied (the REQUIRES rule)
        VehicleSpecification specification =
                new VehicleSpecification(new SpecificationId("SPEC-4"), new CatalogId("CAT-1"));
        when(specificationRepository.findById(new SpecificationId("SPEC-4")))
                .thenReturn(Optional.of(specification));
        doThrow(new RuleViolationException("Option A requires B to be selected."))
                .when(ruleValidation).assertComplete(specification);

        assertThatThrownBy(() -> specificationAppService.finalizeSpecification("SPEC-4"))
                .isInstanceOf(RuleViolationException.class);

        // Finalization aborted: no save and no event
        verify(specificationRepository, never()).save(any());
        verifyNoInteractions(eventPublisher);
    }
}
