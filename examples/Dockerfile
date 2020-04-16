FROM openjdk:11-jre-slim

COPY build/libs/*-all.jar /opt/mqtt/bin/client.jar
COPY config/ /opt/mqtt/config

WORKDIR /opt/mqtt/bin
ENV JAVA_OPTS="-Dcom.sun.management.jmxremote \
               -Dcom.sun.management.jmxremote.authenticate=false \
               -Dcom.sun.management.jmxremote.ssl=false \
               -Dcom.sun.management.jmxremote.port=9090 \
               -Dcom.sun.management.jmxremote.rmi.port=9090 \
               -Djava.rmi.server.hostname=192.168.0.12"
ENTRYPOINT ["java", "-jar", "client.jar"]
