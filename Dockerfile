FROM openjdk:21-jdk-slim

# ENV CLOJURE_VERSION=1.12.0.849
# RUN curl -O https://download.clojure.org/install/linux-install-${CLOJURE_VERSION}.sh && \
#     chmod +x linux-install-${CLOJURE_VERSION}.sh && \
#     ./linux-install-${CLOJURE_VERSION}.sh && \
#     rm linux-install-${CLOJURE_VERSION}.sh

WORKDIR /app

COPY target/meteo_data.jar /app

CMD ["java", "-jar", "meteo_data.jar"]

