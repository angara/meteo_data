FROM eclipse-temurin:25-jdk

WORKDIR /app

COPY target/meteo_data.jar /app

CMD ["java", "-jar", "meteo_data.jar"]
