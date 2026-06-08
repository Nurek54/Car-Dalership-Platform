sequenceDiagram
autonumber

    actor L as Logistyk / Skanner

    box lightblue Adaptery Wejściowe
        participant Rest as YardRestAdapter
    end
    box lightgreen Warstwa Aplikacji
        participant App as YardManagementAppService
    end
    box lavender Adaptery Wyjściowe (Zewnętrzne)
        participant Acl as ImporterIdentityAclPort
        participant Report as DamageReportPort
    end
    box pink Warstwa Dziedziny
        participant Agg as InventoryVehicle
    end
    box lavender Adaptery Wyjściowe (Wewnętrzne)
        participant Repo as VehicleRepository
        participant Bus as EventPublisher
    end

    %% KROK 1: SKANOWANIE KODU
    L ->> Rest: POST /api/yard/receive {vin, isDamaged}
    activate Rest
    Rest ->> App: receiveVehicleOnYard(command)
    activate App

    %% KROK 2: WERYFIKACJA TOŻSAMOŚCI W FABRYCE (ACL)
    App ->> Acl: fetchVehicleIdentity(vin)
    activate Acl

    alt Scenariusz A2: Obcy numer VIN
        Acl -->> App: ForeignVinException / null
        note right of App: Brak pojazdu w bazie Importera.<br/>Blokada procesu.
        App -->> Rest: 403 Forbidden (Błąd tożsamości)
    else Scenariusz Główny i A1: Rozpoznano pojazd
        Acl -->> App: importerData
        deactivate Acl

        note right of App: Otwarcie transakcji bazodanowej

        alt Scenariusz A1: Uszkodzenie w transporcie
            App ->> Agg: createWithDamage(vin, importerData)
            activate Agg
            note right of Agg: Ustawienie VehicleState na TRANSPORT_DAMAGE
            Agg -->> App: vehicle
            deactivate Agg

            %% GENEROWANIE PROTOKOŁU SZKODY
            App ->> Report: generateDamageReport(vin)
            activate Report
            Report -->> App: void
            deactivate Report

            App ->> Repo: save(vehicle)
            App -->> Rest: 202 Accepted (Szkoda zgłoszona, przyjęcie wstrzymane)

        else Scenariusz Główny: Pełne przyjęcie na plac
            App ->> Agg: createVehicle(vin)
            activate Agg
            Agg -->> App: vehicle
            deactivate Agg

            %% KROK 3, 4 i 5: LOGIKA AGREGATU I GENEROWANIE ZDARZENIA
            App ->> Agg: receiveOnYard(importerData)
            activate Agg
            note right of Agg: Ustawienie VehicleState na ON_YARD.<br/>Zapisanie yardEntryDate = now() (Zegar metryki).

            %% DYNAMICZNA INSTANCJACJA OBIEKTU ZDARZENIA (UML)
            create participant Evt as VehicleReceivedOnYardEvent
            Agg ->> Evt: <<create>> new(vin, timestamp)

            Agg ->> Agg: registerEvent(Evt)
            note right of Agg: Zapisanie referencji obiektu w pamięci agregatu
            Agg -->> App: void
            deactivate Agg

            %% ZAPIS I OSTATECZNA SPÓJNOŚĆ
            App ->> Repo: save(vehicle)
            activate Repo
            Repo -->> App: void
            deactivate Repo
            note right of App: Zatwierdzenie transakcji bazodanowej (Commit)

            App ->> Agg: pullDomainEvents()
            activate Agg
            Agg -->> App: List~DomainEvent~
            deactivate Agg

            loop Dla każdego obiektu zdarzenia
                App ->> Bus: publish(event)
                activate Bus
                note right of Bus: Rozgłoszenie zdarzenia PojazdPrzyjetyNaPlac na magistrali
                Bus -->> App: void
                deactivate Bus
            end

            App -->> Rest: 200 OK (Pojazd na placu)
        end
    end

    deactivate App
    Rest -->> L: Odpowiedź wizualna (Skaner)
    deactivate Rest
