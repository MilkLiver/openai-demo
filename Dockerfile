#======================== build container ========================
FROM docker.io/library/maven:3.8.5-openjdk-17 AS builder

MAINTAINER milkliver
#ARG uid=0
#ARG gid=0
USER 0



#======================== add configs and jar ========================
RUN mkdir /workdir
WORKDIR /workdir

COPY ./src /workdir/src
COPY ./pom.xml /workdir/

RUN mvn clean package -DskipTests


#======================== runtime container ========================
FROM registry.access.redhat.com/ubi9/openjdk-17-runtime

MAINTAINER milkliver

ENV PROPERTIES_PATH=/workdir/configs/application.properties

#======================== configure environment ========================
RUN mkdir -p /workdir
RUN mkdir -p /workdir/configs

WORKDIR /workdir

COPY --from=builder /workdir/target/*.jar /workdir/
COPY --from=builder /workdir/src/main/resources/*.properties /workdir/configs/

RUN chmod 777 -Rf /workdir
RUN chmod 744 -Rf /workdir/configs/*


#======================== run ========================
USER 1000

ENTRYPOINT exec ls /workdir/*.jar | xargs -i /bin/java -jar -Dspring.config.location=$PROPERTIES_PATH {}
