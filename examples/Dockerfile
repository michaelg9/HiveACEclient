FROM openjdk:11-jre-slim

COPY build/libs/*-all.jar /opt/mqtt/bin/client.jar
COPY config/ /opt/mqtt/config

WORKDIR /opt/mqtt/bin

ENTRYPOINT ["java", "-jar", "client.jar"]
