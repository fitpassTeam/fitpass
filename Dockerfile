FROM openjdk:17
WORKDIR /app

COPY build/libs/*.jar app.jar

RUN apk add --no-cache curl

ENTRYPOINT ["java", "-jar", "app.jar"]