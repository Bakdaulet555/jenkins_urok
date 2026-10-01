FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/scala-2.13/akka-iot-service.jar /app/service.jar
USER 10001:10001
ENTRYPOINT ["java", "-jar", "/app/service.jar"]
