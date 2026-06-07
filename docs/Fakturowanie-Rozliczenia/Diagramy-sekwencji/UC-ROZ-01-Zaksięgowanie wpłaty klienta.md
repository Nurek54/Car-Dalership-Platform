sequenceDiagram
autonumber

    actor B as Bramka Płatnicza

    box lightblue Adaptery Wejściowe
        participant Rest as BillingRestAdapter
    end

    box lightgreen Warstwa Aplikacji
        participant App as PaymentAppService
    end

    box pink Warstwa Dziedziny
        participant DS as PaymentClassificationService
        participant Agg as Payment
    end

    box lavender Adaptery Wyjściowe
        participant Repo as PaymentRepository
        participant Bus as EventPublisher
    end

    %% KROK 1: INICJACJA
    B ->> Rest: POST /api/payments/webhook
    activate Rest

    Rest ->> App: registerPayment
    activate App

    %% KROK 2: IDENTYFIKACJA I SCENARIUSZE
    alt Scenariusz A1: Nierozpoznany tytuł przelewu
        App ->> Agg: createUnassignedPayment
        activate Agg
        note right of Agg: Ustawienie PaymentStatus na UNASSIGNED
        Agg -->> App: payment
        deactivate Agg
        App ->> Repo: save
        App -->> Rest: 202 Accepted
    else Scenariusz Główny i A2: Zidentyfikowane zamówienie
        note right of App: Otwarcie transakcji bazodanowej

        App ->> Agg: createPayment
        activate Agg
        note right of Agg: Ustawienie PaymentStatus na ASSIGNED
        Agg -->> App: payment
        deactivate Agg

        App ->> DS: calculateRequiredDeposit(orderValue)
        activate DS

        DS -->> App: requiredDeposit
        deactivate DS

        %% KROK 3: KATEGORYZACJA I DYNAMICZNE TWORZENIE OBIEKTU ZDARZENIA
        App ->> Agg: categorizePayment
        activate Agg

        alt Kwota >= Wymagany zadatek (Scenariusz Główny)
            note right of Agg: Ustawienie PaymentCategory na DEPOSIT

            %% DYNAMICZNE TWORZENIE OBIEKTU W UML
            create participant Evt1 as DepositRegisteredEvent
            Agg ->> Evt1: <<create>>

            Agg ->> Agg: registerEvent(Evt1)
            note right of Agg: Dodanie referencji obiektu do prywatnej listy agregatu

        else Kwota < Wymagany zadatek (Scenariusz A2: Niedopłata)
            note right of Agg: Ustawienie PaymentCategory na ADVANCE

            %% DYNAMICZNE TWORZENIE OBIEKTU W UML
            create participant Evt2 as AdvanceRegisteredEvent
            Agg ->> Evt2: <<create>>

            Agg ->> Agg: registerEvent(Evt2)
        end
        Agg -->> App: void
        deactivate Agg

        %% KROK 4: ZAPIS W EWIDENCJI
        App ->> Repo: save
        activate Repo
        Repo -->> App: void
        deactivate Repo
        note right of App: Zatwierdzenie transakcji bazodanowej (Commit)

        %% KROK 5 i 6: PUBLIKACJA OBIEKTÓW ZDARZEŃ
        App ->> Agg: pullDomainEvents
        activate Agg
        Agg -->> App: List~DomainEvent~
        deactivate Agg
        note right of App: Pobranie wygenerowanych obiektów z pamięci agregatu

        loop Dla każdego obiektu zdarzenia
            App ->> Bus: publish
            activate Bus
            Bus -->> App: void
            deactivate Bus
        end

        App -->> Rest: void
    end

    deactivate App
    Rest -->> B: 200 OK
    deactivate Rest
