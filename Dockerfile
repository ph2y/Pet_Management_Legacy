FROM openjdk:8-jdk-alpine
ARG JAR_FILE=target/*.jar
COPY build/Pet-Management-0.0.32-SNAPSHOT.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

