FROM openjdk:21-jdk-slim

WORKDIR /app

COPY target/meteo_data.jar /app

CMD ["java", "-jar", "meteo_data.jar"]

