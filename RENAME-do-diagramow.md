# Ujednolicenie nazw klas do diagramów (Rys. 23/28/37/42/48)

Wykonaj w IntelliJ przez **Shift+F6 (Rename)** na klasie/interfejsie — IDE zmieni atomowo nazwę pliku i wszystkie referencje. Kolejność dowolna, ale najpierw zrób sekcję „2 poprawki sprzeczne", potem czyste renamy.

Legenda: `nazwa w kodzie` → **`nazwa z diagramu`**

---

## 0. Najpierw: 2 zmiany, które są SPRZECZNE z diagramami (moje wcześniejsze błędy z tekstu PDF)

### 0a. Bank port (Rys. 42)
`ExternalBankIntegrationPort` → **`BankIntegrationAcl`** (Shift+F6).

### 0b. Katalog — scal 3 porty z powrotem w jeden (Rys. 28: jeden port `BuildSpecification`)
To NIE jest zwykły rename — to scalenie. Kroki:
1. Utwórz interfejs `salon.catalog.application.port.in.BuildSpecification` z trzema metodami:
   ```java
   SpecificationId startSpecification(String catalogId);
   void addOption(String specificationId, String catalogId, String optionCode);
   void finalizeSpecification(String specificationId);
   ```
2. W `SpecificationAppService` zmień `implements StartConfigurationUseCase, PickOptionUseCase, ConfirmConfigurationUseCase` → `implements BuildSpecification`.
3. W `ConfiguratorRestController` wstrzyknij `BuildSpecification` zamiast trzech portów.
4. Usuń `StartConfigurationUseCase`, `PickOptionUseCase`, `ConfirmConfigurationUseCase` (oraz plik `BuildSpecificationUseCase.java.removed`).

---

## 1. Fakturowanie i Rozliczenia — Rys. 48 (najbliżej zgodności)
Usługi/repozytoria już zgodne ✓ (`DocumentGenerationService`, `PaymentProcessService`, `InvoiceCalculationService`, `SettlementDatabaseRepository`, `DocumentDatabaseRepository`).

| w kodzie | → diagram |
|---|---|
| `GenerateAdvanceUseCase` | **`GenerateAdvance`** |
| `GenerateInvoiceUseCase` | **`GenerateInvoice`** |
| `ProcessPaymentUseCase` | **`ProcessPayment`** |
| `PdfGeneratorPort` | **`PdfGeneration`** |
| `NotificationPort` | **`NotificationGeneration`** |
| `CrmIntegrationPort` (billing) | **`SalesIntegration`** |

## 2. Sprzedaż i CRM — Rys. 23
Repozytoria już zgodne ✓ (`Customer/Offer/OrderDatabaseRepository`), `SalesQueryFacade`, `SalesQueryService` ✓.

| w kodzie | → diagram |
|---|---|
| `SalesAppService` | **`SalesService`** |
| `AcceptOfferUseCase` | **`AcceptOffer`** |
| `ReleaseVehicleUseCase` | **`ReleaseVehicle`** |
| `ScheduleHandoverUseCase` | **`ScheduleHandover`** |
| `ActivateOrderOnDepositUseCase` | **`ActivateOrderOnDeposit`** |
| `StartConfiguratorUseCase` | **`StartConfigurator`** |
| `BillingIntegrationPort` | **`BillingIntegration`** |
| `CatalogIntegrationPort` | **`CatalogIntegration`** |
| `InventoryIntegrationPort` | **`InventoryIntegration`** |

## 3. Katalog i Konfigurator — Rys. 28
(najpierw sekcja 0b)

| w kodzie | → diagram |
|---|---|
| `SpecificationAppService` | **`BuildSpecificationService`** |
| `CatalogAppService` | **`UpdateCatalogService`** |
| `UpdateCatalogUseCase` | **`UpdateCatalog`** |
| `RuleValidationDomainService` | **`RuleValidationService`** |
| `SpecificationRepository` | **`SpecificationDatabaseRepository`** |
| `CatalogRepository` | **`CatalogDatabaseRepository`** |
| `ImporterApiPort` | **`ImporterACL`** |

## 4. Inwentarz i Logistyka — Rys. 37

| w kodzie | → diagram |
|---|---|
| `InventoryManagementAppService` | **`InventoryManagementService`** |
| `ReserveVehicleUseCase` | **`ReserveVehicle`** |
| `ReceiveVehicleUseCase` | **`ReceiveVehicle`** |
| `PrepareForHandoverUseCase` | **`PrepareForHandover`** |
| `ReleaseInventoryUseCase` | **`ReleaseVehicle`** |
| `InventoryRepository` | **`VehicleDatabaseRepository`** |

## 5. Finansowanie — Rys. 42
(najpierw sekcja 0a)

| w kodzie | → diagram |
|---|---|
| `FinancingAppService` | **`ProcessFinancingService`** |
| `FinancingRequestUseCase` | **`ProcessFinancing`** |
| `FinancingDatabaseRepository` | **`FinancingApplicationDatabaseRepository`** |
| `CrmIntegrationPort` (financing) | **`SalesIntegration`** |

## 6. Wspólne (common) — występuje na każdym diagramie jako `EventPublisher`
`EventPublisherPort` → **`EventPublisher`** (Shift+F6). UWAGA: to klasa współdzielona — rename dotknie wszystkich kontekstów (to jest pożądane).

---

## Uwagi / decyzje do podjęcia (diagram vs kod)

1. **Finansowanie — jeden port vs dwa.** Diagram (Rys. 42) pokazuje JEDEN port `ProcessFinancing` i NIE pokazuje osobnego portu/kontrolera decyzji. W kodzie dodaliśmy `IssueDecisionUseCase` + `FinancingDecisionRestController` (UC-FIN-02). Aby było 1:1 z diagramem, trzeba by scalić `processDecision` do `ProcessFinancing` i usunąć `IssueDecisionUseCase` + `FinancingDecisionRestController`. Decyzja należy do Ciebie (diagram uproszczony vs pełny UC-FIN-02).

2. **Inwentarz — `ReleaseReservationUseCase`, `OrderFactoryVehicleUseCase`, `SynchronizeSpecificationUseCase`** nie mają osobnych pudełek na Rys. 37 (diagram pokazuje tylko 4 porty). Zostaw jak są lub dostosuj wg uznania.

3. **Inwentarz — porty wyjściowe na Rys. 37 wyglądają na pomyłki copy-paste** (`ImporterACL ← PdfGenerator`, `CatalogIntegration ← CatalogExternalAPI` w kontekście inwentarza). W kodzie masz `FactoryIntegrationAclPort` i `SpecificationReadModelPort` (świadome ulepszenie: read model zamiast synchronicznej integracji). Sugeruję NIE zmieniać ich na siłę — diagram tu jest niespójny. Do potwierdzenia z zespołem.

4. **`ReceiveSpecificationUseCase` (Sprzedaż)** — diagram Rys. 23 nie pokazuje portu generowania oferty (UC-CRM-02). Twój `ReceiveSpecificationUseCase` jest „ponad diagram". Zostaw albo usuń wg uznania.

5. **`ExpireOutdatedOffer`** na Rys. 23 jest pudełkiem portu, a w kodzie to metoda `expireOutdatedOffers` w usłudze (uruchamiana cronem). Opcjonalnie wydziel port `ExpireOutdatedOffer`.

6. **Zielone pudełka** na diagramach (`DBAdapter`, `RabbitMq`, `SalesQueryService`, `BankService`, `CatalogExternalAPI`, `PdfGenerator`, `ImporterService`, `NotificationGenerator`, `…ExternalAPI`) to ogólne/poglądowe nazwy adapterów infrastruktury — nie są dosłownymi nazwami klas (np. jeden `DBAdapter` = wiele `*DatabaseAdapter`/`*JpaEntity`). Nie wymagają zmian 1:1.

---

Po wszystkich renamach: **`mvn test`** (zwłaszcza `HexagonalArchitectureTest` — używa wzorców `..application.port..`, `..domain.event..`, więc renamy klas go nie ruszą).
