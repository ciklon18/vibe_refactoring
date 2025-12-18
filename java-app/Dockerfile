FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app

COPY pom.xml /app/
RUN mvn -B -ntp dependency:go-offline

COPY src /app/src
RUN mvn -B -ntp clean package -DskipTests

FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
