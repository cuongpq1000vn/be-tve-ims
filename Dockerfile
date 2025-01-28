FROM maven:3.9.9-sapmachine-22 AS build

WORKDIR /app

COPY . .

USER root

RUN mvn install -Dmaven.test.skip=true && \ 
    mvn package -Dmaven.test.skip=true

FROM openjdk:23-ea-22-jdk-bookworm

WORKDIR /app

ENV APP_NAME=triviet-0.0.1-SNAPSHOT.jar

COPY --from=build /app/target/${APP_NAME} .
COPY --from=build /app/client_secret.json .
COPY --from=build /app/service_user.json .
EXPOSE 8080

CMD ["sh", "-c" , "java -jar $APP_NAME"]