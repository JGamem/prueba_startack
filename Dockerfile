FROM eclipse-temurin:20-jdk-alpine as build

WORKDIR /tmp/build
COPY . .

RUN chmod +x ./gradlew
RUN ./gradlew shadowJar --no-daemon

FROM eclipse-temurin:20-jre-alpine
WORKDIR /app
COPY --from=build /tmp/build/build/libs/kotlin-htmx-all.jar ./app.jar
COPY .env.default .

EXPOSE 8080
ENV TZ="America/Guatemala"

HEALTHCHECK --interval=30s --timeout=10s --retries=3 CMD wget -q --spider http://localhost:8080/health || exit 1

RUN addgroup -S startrack && adduser -S startrack -G startrack
USER startrack

ENTRYPOINT ["java", "-jar", "app.jar"]