#!/usr/bin/env bash

# Configuration variables
PROJECT_NAME=CyrusHABDaemon
PROJECT_VERSION=0.1.0


SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd)"
DIST_ZIP="${SCRIPT_DIR}/${PROJECT_NAME}/build/distributions/${PROJECT_NAME}-${PROJECT_VERSION}.zip"
TARGET="${SCRIPT_DIR}/build_temp"

# Clean target first.
if [ -d ${TARGET} ]; then
    echo "Cleaning target directory: ${TARGET} ..."
    rm -f -R -v ${TARGET}
fi

# Check for distribution. Run build if necessary.
if [ ! -f ${DIST_ZIP} ]; then
    echo
    echo "WARNING: Distribution not found. Attempting to build first..."
    echo "File not found: ${DIST_ZIP}"
    echo

    BUILD_SCRIPT="${SCRIPT_DIR}/build-app.sh"
    if [ -f ${BUILD_SCRIPT} ]; then
        ${BUILD_SCRIPT}
        if [ ! -f ${DIST_ZIP} ]; then
            echo "ERROR: A build error occurred. Cannot continue."
            exit 1
        fi
    else
        echo "ERROR: Build script not found. Cannot continue."
        exit 1
    fi
fi

# Unpack the distribution to target.
echo
echo "Unpacking distribution..."
echo
[ ! -d ${TARGET} ] || mkdir ${TARGET}
unzip ${DIST_ZIP} -d ${TARGET}
if [ ! -d "${TARGET}/${PROJECT_NAME}-${PROJECT_VERSION}" ]; then
    echo "ERROR: Extraction failed. Cannot continue."
    exit 1
fi

# Copy launcher script to bin and set executable.
cp -v "${SCRIPT_DIR}/${PROJECT_NAME}/src/main/scripts/hab_daemon_launcher.sh" "${TARGET}/${PROJECT_NAME}-${PROJECT_VERSION}/bin/"
LAUNCHER="${TARGET}/${PROJECT_NAME}-${PROJECT_VERSION}/bin/hab_daemon_launcher.sh"
chmod -v +rx ${LAUNCHER}

# Launch the daemon.
echo
echo "Running daemon launcher..."
echo
export PROJECT_NAME PROJECT_VERSION
${LAUNCHER} start