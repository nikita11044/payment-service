FROM eclipse-temurin:21-jre-alpine

ARG JAR_FILE=payment-service-app-0.0.1-SNAPSHOT.jar

WORKDIR /app

COPY target/${JAR_FILE} app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
