FROM eclipse-temurin:21-jdk-jammy

ENV TZ=America/Fortaleza
RUN apt-get update && \
    apt-get install -y postgresql-client && \
    ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && \
    echo $TZ > /etc/timezone && \
    rm -rf /var/lib/apt/lists/* \

WORKDIR /app

COPY target/fidelity-0.0.1-SNAPSHOT.jar app.jar

ARG SPRING_DATASOURCE_URL
ARG SPRING_DATASOURCE_USERNAME
ARG SPRING_DATASOURCE_PASSWORD
ARG SPRING_SERVER_PORT

ENV SPRING_DATASOURCE_PASSWORD=${SPRING_DATASOURCE_PASSWORD} \
    SPRING_DATASOURCE_URL=${SPRING_DATASOURCE_URL} \
    SPRING_DATASOURCE_USERNAME=${SPRING_DATASOURCE_USERNAME} \
    SPRING_SERVER_PORT=${SPRING_SERVER_PORT}

EXPOSE 8080

ENTRYPOINT ["java", "-Duser.timezone=America/Fortaleza", "-jar", "app.jar"]