#!/usr/bin/env bash

PROJECT_NAME=CyrusHABDaemon
CYRUSHAB_PID_FILE=/tmp/${PROJECT_NAME}.pid
CYRUSHAB_LAUNCHER=/opt/cyrushabdaemon/bin/hab_daemon_launcher.sh

is_service_running() {
    if [ -f "${CYRUSHAB_PID_FILE}" ]; then
        return 0
    fi
    return 1
}

stop_service() {
    if [ -f "${CYRUSHAB_LAUNCHER}" ]; then
        "${CYRUSHAB_LAUNCHER}" stop
    fi
}