FROM openjdk:21-jdk-slim

WORKDIR /app

COPY ./build/libs/amine-api-all.jar /app/amine-api.jar

EXPOSE 8080

CMD ["java", "-jar", "amine-api.jar"]