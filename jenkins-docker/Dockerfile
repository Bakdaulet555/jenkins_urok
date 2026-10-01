FROM eclipse-temurin:21-jre

WORKDIR /jenkins

COPY jenkins.war /jenkins/jenkins.war

EXPOSE 8080
EXPOSE 50000

CMD ["java", "-jar", "/jenkins/jenkins.war"]