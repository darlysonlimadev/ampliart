FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /app

COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN chmod +x mvnw
RUN ./mvnw -q -DskipTests dependency:go-offline

COPY src/ src/
RUN ./mvnw -q -DskipTests package
RUN set -eux; \
    JAR_FILE="$(find /app/target -maxdepth 1 -name '*.jar' ! -name '*.jar.original' | head -n 1)"; \
    cp "$JAR_FILE" /app/app.jar

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY --from=build /app/app.jar /app/app.jar
RUN addgroup --system spring && adduser --system spring --ingroup spring && chown spring:spring /app/app.jar
USER spring:spring

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV DB_URL=jdbc:postgresql://postgres:5432/ampliart
ENV DB_USERNAME=postgres
ENV DB_PASSWORD=postgres

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
