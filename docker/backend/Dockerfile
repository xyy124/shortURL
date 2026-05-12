FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build
COPY pom.xml ./
COPY short-url-common/pom.xml short-url-common/
COPY short-url-core/pom.xml short-url-core/
COPY short-url-api/pom.xml short-url-api/
COPY short-url-admin/pom.xml short-url-admin/
COPY short-url-web/pom.xml short-url-web/

RUN mvn dependency:go-offline -B

COPY . .
RUN mvn package -DskipTests -B

FROM eclipse-temurin:17-jre

WORKDIR /app
COPY --from=builder /build/short-url-web/target/*.jar app.jar

EXPOSE 8060
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=prod"]
