FROM java:8
FROM maven:alpine

WORKDIR /app

COPY . /app

RUN mvn --version
RUN mvn clean install -DskipTests
LABEL maintainer="billydev@gmail.com"
ENTRYPOINT ["java","-jar","/app/target/30780Exp065_JobEngineForOrange-0.0.1-SNAPSHOT-jar-with-dependencies.jar"]


