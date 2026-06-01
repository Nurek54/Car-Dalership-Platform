## Wymagania

- JDK 17+
- Docker Desktop (tylko dla wariantu z RabbitMQ)

## Uruchomienie — OFFLINE (bez brokera)

Nic nie instalujesz, nie potrzebujesz Dockera. W IntelliJ odpal `main()` jednej z klas:

- `salon.bootstrap.OfflineDemo` — cała domena trzech kontekstów, zdarzenia tylko logowane.
- `salon.bootstrap.SalonDemo` — choreografia zadatek → aktywacja zamówienia, in-process.

## Uruchomienie — z RabbitMQ (przez prawdziwą kolejkę)

1. Uruchom Docker Desktop, a potem broker:

   ```
   docker compose up -d rabbitmq
   docker compose ps          # poczekaj aż status = healthy
   ```

2. Odpal `main()` klasy `salon.bootstrap.MessagingDemo`.

   W konsoli na końcu powinno być: `Stan zamówienia ...: IN_PROGRESS`
   (zadatek przeszedł przez kolejkę z Rozliczeń do Sprzedaży i aktywował zamówienie).

3. Panel brokera: http://localhost:15672 (login `guest` / `guest`).

4. Sprzątanie:

   ```
   docker compose down
   ```

## Testy

```
mvn test
```

## Build w pełni kontenerowy (opcjonalnie, bez Javy/Mavena na hoście)

```
docker compose up --build
```

Zbuduje fat jar przez Maven, podniesie RabbitMQ i odpali `MessagingDemo` w kontenerze.
`MessagingDemo` to demo — wykona przepływ i zakończy się (kontener `app` wyjdzie z kodem 0).
