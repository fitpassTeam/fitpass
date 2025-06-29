FROM openjdk:17-jdk
WORKDIR /app

COPY build/libs/*.jar app.jar

RUN apt-get update && apt-get install -y curl

ENTRYPOINT ["java", "-jar", "app.jar"]