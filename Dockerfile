FROM adoptopenjdk/openjdk11:jre-11.0.8_10-alpine

ARG JAR_FILE=*.jar
ARG http_proxy
ENV HTTP_PROXY=$http_proxy
ENV http_proxy=$HTTP_PROXY
ARG https_proxy
ENV HTTPS_PROXY=$https_proxy
ENV https_proxy=$HTTPS_PROXY
USER root
RUN mkdir -p /app/config
RUN mkdir -p /app/certificates
RUN mkdir -p /app/logs
RUN mkdir -p /app/ca-certificates
RUN apk update && apk add apache2-utils
COPY target/${JAR_FILE} /app/app.jar

COPY configs/logging/logback-spring.xml /app
COPY scripts/start-app.sh /app
COPY scripts/wait-for.sh /app
COPY ca-certificates/onap-ca.crt /app/ca-certificates/onap-ca.crt
#RUN chown -R so:so /app
#USER so
# Springboot configuration (required)
VOLUME /app/config
#  Root certificates (optional)
VOLUME /app/ca-certificates
WORKDIR /app
ENTRYPOINT ["/app/start-app.sh"]

