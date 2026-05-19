FROM gradle:9.1-jdk21 AS build

WORKDIR /app
COPY . .
RUN gradle installDist --no-daemon

FROM eclipse-temurin:21-jre

WORKDIR /app
ENV OPS_DB_PATH=/data/ops-hub.db
ENV PORT=8080

RUN mkdir -p /data
COPY --from=build /app/build/install/personal-ops-hub /app

EXPOSE 8080
ENTRYPOINT ["./bin/personal-ops-hub"]
