
FROM eclipse-temurin:17-jdk AS build

WORKDIR /app

COPY src/ ./src/

RUN mkdir -p out && \
    javac --add-modules jdk.httpserver -d out src/*.java

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=build /app/out ./out
COPY web ./web

RUN mkdir -p generated

CMD ["java", "--add-modules", "jdk.httpserver", "-cp", "out", "webserver"]
