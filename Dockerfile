FROM eclipse-temurin:25-jdk AS build

WORKDIR /workspace

COPY .mvn .mvn
COPY maven maven
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw -B -s maven/settings-docker.xml -DskipTests dependency:go-offline

COPY src src
RUN ./mvnw -B -s maven/settings-docker.xml -DskipTests package

FROM eclipse-temurin:25-jre

WORKDIR /app

ENV SPRING_PROFILES_ACTIVE=prod

COPY --from=build /workspace/target/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
