#!/usr/bin/env bash

# Comment this line to disable debug mode.
DEBUG=1

# If we weren't called from run_daemon.sh, these might not be set.
if [ -z "${PROJECT_NAME}" ]; then
    PROJECT_NAME=CyrusHABDaemon
fi

if [ -z "${PROJECT_VERSION}" ]; then
    PROJECT_VERSION=0.1.0
fi

echo
echo "${PROJECT_NAME} v${PROJECT_VERSION} Service Control"
echo

# Look for JRE executable. If we can't find it, then fail.
JAVA_EXEC="$( which java )"
if [[ -z "${JAVA_EXEC}" ]]; then
    echo
    echo "ERROR: Java runtime not found. Please install JRE 1.8x or higher to continue."
    echo
    exit 1
fi

# Look for JSVC executable. If we can't find it, then fail.
JSVC_EXECUTABLE="$( which jsvc )"
if [[ -z "${JSVC_EXECUTABLE}" ]]; then
    echo
    echo "ERROR: Apache JSVC not installed."
    echo "Please install JSVC and re-run this script to continue."
    echo "Using Homebrew on Mac: brew install jsvc"
    echo "Using Apt on Linux: apt-get install jsvc"
    echo
    exit 1
fi

if [ -z "${JSVC_USER}" ]; then
    JSVC_USER="$USER"
fi

DIST_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )/../" && pwd )"
LIB_DIR="${DIST_DIR}/lib"

# **** ADJUST DEPENDENCY VERSIONS HERE AS NEEDED ****
JAVA_CLASSPATH="${LIB_DIR}/${PROJECT_NAME}-${PROJECT_VERSION}.jar"
JAVA_CLASSPATH+=":${LIB_DIR}/CyrusHABLib-${PROJECT_VERSION}.jar"
JAVA_CLASSPATH+=":${LIB_DIR}/annotations-16.0.2.jar"
JAVA_CLASSPATH+=":${LIB_DIR}/commons-daemon-1.0.15.jar"
JAVA_CLASSPATH+=":${LIB_DIR}/commons-lang3-3.8.1.jar"
JAVA_CLASSPATH+=":${LIB_DIR}/json-simple-1.1.jar"
JAVA_CLASSPATH+=":${LIB_DIR}/slf4j-api-1.7.25.jar"
JAVA_CLASSPATH+=":${LIB_DIR}/logback-core-1.2.3.jar"
JAVA_CLASSPATH+=":${LIB_DIR}/logback-classic-1.2.3.jar"
JAVA_CLASSPATH+=":${LIB_DIR}/org.eclipse.paho.client.mqttv3-1.2.0.jar"

JAVA_MAIN_CLASS="net.cyrusbuilt.cyrushab.daemon.HABDaemon"
JAVA_OPTS="-Ddistribution.dir=${DIST_DIR}"

# If JAVA_HOME is not already defined, use our utility class to try and find it.
if [ -z "${JAVA_HOME}" ]; then
    export JAVA_HOME="$( ${JAVA_EXEC} -cp "${JAVA_CLASSPATH}" -server \
        net.cyrusbuilt.cyrushab.daemon.GetProperty java.home )"
fi

# If JAVA_HOME ends with '/jre' then trim that last part of the path off. Otherwise, JSVC blows up.
if [[ ${JAVA_HOME} == */jre ]]; then
    JAVA_HOME=${JAVA_HOME::${#JAVA_HOME}-4}
fi

# If we aren't in debug mode, redirect stdout and stderr to their appropriate log files.
PID_FILE=/tmp/${PROJECT_NAME}.pid
if [ -z ${DEBUG} ]; then
    LOG_OUT=/tmp/${PROJECT_NAME}.out
    LOG_ERR=/tmp/${PROJECT_NAME}.err
fi

# Use JSVC to control the daemon.
do_exec() {
    echo
    echo "CLASSPATH: ${JAVA_CLASSPATH}"
    echo "MAIN CLASS: ${JAVA_MAIN_CLASS}"
    echo "OPTIONS: ${JAVA_OPTS}"
    echo "JAVA HOME: ${JAVA_HOME}"
    echo "JAVA USER: ${JSVC_USER}"

    ARGS="-server -nodetach -home "${JAVA_HOME}" -cp "${JAVA_CLASSPATH}" -user ${JSVC_USER} -pidfile ${PID_FILE} "
    if [ -z ${DEBUG} ]; then
        ARGS+="-outfile ${LOG_OUT} -errfile ${LOG_ERR}"
    else
        ARGS+="-debug -outfile &1 -errfile &2"
    fi

    ARGS+=" $1 ${JAVA_OPTS} ${JAVA_MAIN_CLASS}"

    ${JSVC_EXECUTABLE} ${ARGS}
}

# Start the daemon if not already running.
do_start() {
    if [ -f "${PID_FILE}" ]; then
        echo "Daemon already running." >&2
        exit 1
    fi
    do_exec
}

# Stop the daemon if running and cleanup PID file if left behind.
do_stop() {
    if [ ! -f "${PID_FILE}" ]; then
        echo "Daemon already stopped." >&2
        return
    fi

    do_exec "-stop"
    if [ -f "${PID_FILE}" ]; then
        rm -f "${PID_FILE}"
    fi
}

# Print usage and exit.
print_usage() {
    echo "usage: hab_daemon_launcher {start|stop|restart}" >&2
    exit 3
}

# Check cli args and perform appropriate task.
case "$1" in
    start)
        do_start
            ;;
    stop)
        do_stop
            ;;
    restart)
        do_stop
        do_start
            ;;
    *)
        print_usage
            ;;
esac