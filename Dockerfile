FROM gradle:7.0.2-jdk16 AS build
COPY --chown=gradle:gradle . /home/gradle/rainflake
WORKDIR /home/gradle/rainflake
RUN gradle clean build shadowJar --no-daemon

FROM openjdk:16-slim
ENV RF_PORT=8080
ENV RF_EPOCH=1577836800000
ENV RF_NODEID=1
ENV RF_UNUSED_BITS=1
ENV RF_EPOCH_BITS=41
ENV RF_NODE_ID_BITS=10
ENV RF_SEQUENCE_BITS=12

EXPOSE ${RF_PORT}

RUN mkdir /app

COPY --from=build /home/gradle/rainflake/app/build/libs/shadow.jar /app/rainflake.jar
WORKDIR /app

ENTRYPOINT ["java", "-jar", "rainflake.jar", "env"]