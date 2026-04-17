#!/bin/sh

if [ `id -u` = 0 ]
then
    # Install certificates found in the /app/ca-certificates volume, if any.

    needUpdate=FALSE

    for certificate in `ls -1 /app/ca-certificates`; do
        echo "Installing $certificate in /usr/local/share/ca-certificates"
        cp /app/ca-certificates/$certificate /usr/local/share/ca-certificates/$certificate
        needUpdate=TRUE
    done

    if [ $needUpdate = TRUE ]; then
        update-ca-certificates --fresh
    fi

    # Re-exec this script as the 'onap' user.
    this=`readlink -f $0`
    exec su so -c  "$this"
fi

touch /app/app.jar

if [ ! -z "$DB_HOST" -a -z "$DB_PORT" ]; then
    export DB_PORT=3306
fi

if [ -z "${CONFIG_PATH}" ]; then
    export CONFIG_PATH=/app/config/override.yaml
fi

if [ -z "${LOG_PATH}" ]; then
    export LOG_PATH="logs/${APP}"
fi

if [ "${SSL_DEBUG}" = "log" ]; then
    export SSL_DEBUG="-Djavax.net.debug=all"
else
    export SSL_DEBUG=
fi

# Set java keystore and truststore options, if specified in the environment.

jksargs=

if [ ! -z "${KEYSTORE}" ]; then
    jksargs="$jksargs -Dmso.load.ssl.client.keystore=true"
    jksargs="$jksargs -Djavax.net.ssl.keyStore=$KEYSTORE"
    jksargs="$jksargs -Djavax.net.ssl.keyStorePassword=${KEYSTORE_PASSWORD}"
fi

if [ ! -z "${TRUSTSTORE}" ]; then
    jksargs="$jksargs -Djavax.net.ssl.trustStore=${TRUSTSTORE}"
    jksargs="$jksargs -Djavax.net.ssl.trustStorePassword=${TRUSTSTORE_PASSWORD}"
fi

if [ -z "${ACTIVE_PROFILE}" ]; then
    export ACTIVE_PROFILE="basic"
fi

# Configure OpenTelemetry Java Agent
otel_agent_args=""
if [ "${OTEL_ENABLED}" = "true" ] && [ -f "/app/opentelemetry-javaagent.jar" ]; then
    otel_agent_args="-javaagent:/app/opentelemetry-javaagent.jar"
    echo "OpenTelemetry agent enabled"

    # Set default OTEL service name if not provided
    if [ -z "${OTEL_SERVICE_NAME}" ]; then
        export OTEL_SERVICE_NAME="${APP:-so-cnf-adapter}"
    fi

    # Set default OTEL resource attributes
    if [ -z "${OTEL_RESOURCE_ATTRIBUTES}" ]; then
        export OTEL_RESOURCE_ATTRIBUTES="service.name=${OTEL_SERVICE_NAME},service.namespace=onap"
    fi

    echo "OpenTelemetry Service Name: ${OTEL_SERVICE_NAME}"
    echo "OpenTelemetry Exporter Endpoint: ${OTEL_EXPORTER_OTLP_ENDPOINT:-not set}"
fi

jvmargs="-XX:MaxRAMPercentage=${MAX_RAM_PERCENTAGE:-75} ${otel_agent_args} ${JVM_ARGS} -Dspring.profiles.active=${ACTIVE_PROFILE} -Djava.security.egd=file:/dev/./urandom -Dlogs_dir=${LOG_PATH} -Dlogging.config=/app/logback-spring.xml $jksargs -Dspring.config.additional-location=$CONFIG_PATH ${SSL_DEBUG} ${DISABLE_SNI}"


read_properties(){
    while IFS="=" read -r key value; do
        case "${key}" in
          '#'*) ;;
             *)
               eKey=$(echo $key | tr '[:lower:]' '[:upper:]')
               export "$eKey"="$value"
        esac
    done <<-EOF
	$1
	EOF
}

if [ -n "${AAF_SSL_CERTS_ENABLED}" ]; then
read_properties "$(head -n 4 /app/certs/.passphrases)"
fi

echo "JVM Arguments: ${jvmargs}"

java ${jvmargs} -jar app.jar
rc=$?

echo "Application exiting with status code $rc"

if [ ! -z "${EXIT_DELAY}" -a "${EXIT_DELAY}" != 0 ]; then
    echo "Delaying $APP exit for $EXIT_DELAY seconds"
    sleep $EXIT_DELAY
fi

exit $rc
