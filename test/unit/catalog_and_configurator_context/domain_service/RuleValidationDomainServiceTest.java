package unit.catalog_and_configurator_context.domain_service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import salon.catalog.application.port.out.CatalogRepository;
import salon.catalog.domain.model.catalog.CatalogId;
import salon.catalog.domain.model.catalog.CatalogOption;
import salon.catalog.domain.model.catalog.OptionCode;
import salon.catalog.domain.model.catalog.ProductCatalog;
import salon.catalog.domain.model.specification.VehicleSpecification;
import salon.catalog.domain.service.RuleValidationDomainService;
import salon.shared.model.Money;
import salon.shared.model.SpecificationId;

import java.util.Optional;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RuleValidationDomainServiceTest {

    @Mock
    private CatalogRepository catalogRepository;

    @InjectMocks
    private RuleValidationDomainService ruleValidationService;

    @Test
    void shouldSuccessfullyAddOptionWhenDomainServiceValidatesItAgainstCatalog() {
        // Arrange
        OptionCode manualGearbox = new OptionCode("MANUAL_GEARBOX");
        CatalogId catalogId = new CatalogId("CAT-2026");

        ProductCatalog mockCatalog = ProductCatalog.createActive("MY_2026");
        // Opcja musi istnieć w cenniku — serwis dziedzinowy zleca agregatowi pełną walidację.
        mockCatalog.addOption(new CatalogOption(manualGearbox, Money.of(0, "PLN")));
        // Udajemy, że baza danych zwraca nam poprawny cennik
        when(catalogRepository.findById(catalogId)).thenReturn(Optional.of(mockCatalog));

        VehicleSpecification specification = new VehicleSpecification(
                new SpecificationId("SPEC-001"),
                catalogId
        );

        // Act
        // Nie wywołujemy metody na agregacie, tylko prosimy Usługę Dziedziny o koordynację procesu!
        ruleValidationService.validateAndAddOption(specification, manualGearbox);

        // Assert
        // Upewniamy się, że usługa przekazała opcję do agregatu po pobraniu cennika
        assertThat(specification.getSelectedOptions()).contains(manualGearbox);
        verify(catalogRepository, times(1)).findById(catalogId);
    }
}