FROM openjdk:11-jdk-slim

COPY target/*.jar app.jar

COPY INPUT/IDs.txt IDs.txt

ENTRYPOINT ["java","-jar","/app.jar"]
