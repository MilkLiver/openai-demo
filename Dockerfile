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

ENV WORK_PATH=/home/default

#======================== configure environment ========================
RUN mkdir -p /home/default
RUN mkdir -p /home/default/configs

COPY --from=builder /workdir/target/*.jar $WORK_PATH/
COPY --from=builder /workdir/src/main/resources/*.properties $WORK_PATH/configs/


#======================== run ========================

ENTRYPOINT exec ls $WORK_PATH/*.jar | xargs -i /bin/java -jar -Dspring.config.location=$WORK_PATH/configs/application.properties {}
