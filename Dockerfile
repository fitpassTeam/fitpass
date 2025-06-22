FROM openjdk:17
WORKDIR /app

# 빌드된 jar 복사
COPY build/libs/*.jar app.jar

# 두 개 프로퍼티 파일 복사
COPY src/main/resources/application.properties /app/application.properties
COPY src/main/resources/application-docker.properties /app/application-docker.properties

# 도커 환경에서는 application-docker.properties를 우선으로 사용하도록 실행
ENTRYPOINT ["java", "-jar", "app.jar", "--spring.config.location=file:/app/application-docker.properties,file:/app/application.properties"]