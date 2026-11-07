# https://hub.docker.com/_/maven
# Build stage
FROM maven:3.10.0-eclipse-temurin-17 AS build

WORKDIR /build

# only needed for this laptop that has zscaler configured
# COPY zscaler-root-ca.cer /tmp/zscaler-root-ca.cer
RUN --mount=type=secret,id=zscaler_root_ca,required=true keytool -importcert -noprompt -trustcacerts -alias zscaler-root-ca -file /run/secrets/zscaler_root_ca -keystore /opt/java/openjdk/lib/security/cacerts -storepass changeit

COPY pom.xml .
COPY src ./src
RUN mvn -B -ntp clean package


# Runtime stage
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

COPY --from=build /build/target/capstone-0.0.1-SNAPSHOT.jar app.jar
USER 10001:10001
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
