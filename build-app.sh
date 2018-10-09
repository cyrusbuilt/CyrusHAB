#!/usr/bin/env bash

# Get script dir.
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd)"
GRADLE_SCRIPT=${SCRIPT_DIR}/gradlew

echo
if [ ! -f ${GRADLE_SCRIPT} ]; then
    echo "ERROR: Gradle script not found!"
    exit -1
fi

${GRADLE_SCRIPT} clean check assemble --rerun-tasks --full-stacktrace --continue
exit $?