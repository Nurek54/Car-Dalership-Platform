# --- etap budowania: Maven + JDK 17 ---
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

# najpierw sam pom -> warstwa z zależnościami cache'uje się, dopóki pom się nie zmieni
COPY pom.xml .
RUN mvn -q -e -B dependency:go-offline

# potem źródła i build fat jara (testy pomijamy — to unit testy, odpalisz je na hoście `mvn test`)
COPY src ./src
RUN mvn -q -e -B clean package -DskipTests

# --- etap uruchomieniowy: samo JRE, lekki obraz ---
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/target/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]